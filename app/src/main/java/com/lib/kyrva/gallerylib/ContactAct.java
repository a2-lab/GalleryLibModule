package com.lib.kyrva.gallerylib;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ContactAct extends Activity {

  SharedPreferences sPrefs;
  ListView contactsList;
  List<String> contactNames, contactEmails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	  this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.contacts);
	  contactsList=(ListView) findViewById(R.id.contactsList);
	  contactNames=new ArrayList<String>();
	  contactEmails=new ArrayList<String>();
	  ContentResolver cr=getContentResolver();
	  Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
		  null, null, null, null);
	  if (cur.getCount() > 0) {
		while (cur.moveToNext()) {
		  String id = cur.getString(
			  cur.getColumnIndex(ContactsContract.Contacts._ID));
		  String name = cur.getString(
			  cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		  if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
			Cursor emailCur = cr.query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
				new String[]{id}, null);
			while (emailCur.moveToNext()) {
			  // This would allow you get several email addresses
			  // if the email addresses were stored in an array
			  String email = emailCur.getString(
				  emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
			  contactNames.add(name);
			  contactEmails.add(email);
			  String emailType = emailCur.getString(
				  emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
			}

			emailCur.close();
		  }
		}
	  }
	  ContactsInfoAdapter adapter=new ContactsInfoAdapter(this, android.R.layout.simple_list_item_1);
	  contactsList.setAdapter(adapter);
	  contactsList.setOnItemClickListener(new OnItemClickListener(){
		@Override
		public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l){
		  sPrefs=getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
			String path=sPrefs.getString("path", "a");
		  	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		  	emailIntent.setType("application/image");
		  emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{contactEmails.get(i)});
		  	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"MegaPhoto");
		  	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
		  	emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+path));
		  	startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		  finish();
		}
	  });
    }

  private class ContactsInfoAdapter extends ArrayAdapter{

	Context context;
	LayoutInflater inflater;

	public ContactsInfoAdapter(final Context context, final int resource){
	  super(context, resource);
	  this.context=context;
	  inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override public int getCount(){
	  return contactEmails.size();
	}

	@Override public View getView(final int position, final View convertView, final ViewGroup parent){
	  View view=convertView;
	  if (view==convertView){
		view=inflater.inflate(R.layout.contact_items, parent, false);
	  }
	  TextView nameTView=(TextView) view.findViewById(R.id.itemName);
	  TextView emailTView=(TextView) view.findViewById(R.id.itemEmail);
	  nameTView.setText(String.valueOf(contactNames.get(position)));
	  emailTView.setText(String.valueOf(contactEmails.get(position)));
	  return view;
	}
  }
}
