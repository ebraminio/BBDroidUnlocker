package ir.irtci.bbdroidunlocker;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class BBDroidUnlockerActivity extends Activity implements
		AdapterView.OnItemSelectedListener {
	/** Called when the activity is first created. */
	Button calcButton;
	EditText imeiEditText;
	Spinner mepSpinner;
	TextView unlockCodeTextView;
	BBDroidUnlocker bbDroidUnlocker;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			String message = "", title = "";
			String unlockCode = unlockCodeTextView.getText().toString();
			if (unlockCode.equals("")) {
				title = "Not successful";
				message = "Please enter an IMEI and set MEP version.";
			} else {
				try {
					FileOutputStream outStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/unlock.txt");
					outStream.write(unlockCode.getBytes());
					outStream.flush();
					outStream.close();
					
					title = "Successful";
					message = "Writing unlock code to unlock.txt in your sdcard, was successful.";
				} catch (Exception e) {
					title = "Not successful";
					message = "Operation was not successful.";
				}
			}
			
			new AlertDialog.Builder(this)
				.setMessage(message)
				.setTitle(title)
				.setIcon(R.drawable.icon)
				.create()
					.show();
			break;
		case R.id.menu_exit:
			finish();
			break;
		}
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		imeiEditText = (EditText) findViewById(R.id.imeiEditText);
		mepSpinner = (Spinner) findViewById(R.id.mepSpinner);
		unlockCodeTextView = (TextView) findViewById(R.id.unlockCodeTextView);

		findViewById(R.id.irtciLogo).setOnClickListener(
				new LinkOnClickListener(
						"http://www.irtci.ir/forum"));
		
		findViewById(R.id.gpgLogo).setOnClickListener(
				new LinkOnClickListener(
						"http://www.gpgindustries.com"));
		
		bbDroidUnlocker = new BBDroidUnlocker();
		try {
			bbDroidUnlocker.loadMepsList(getResources().openRawResource(R.raw.meps));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<String> sortedMepsList = new ArrayList<String>(bbDroidUnlocker.mepsMap.keySet());
		sortedMepsList.add("");
		Collections.sort(sortedMepsList);
		

		mepSpinner.setOnItemSelectedListener(this);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, sortedMepsList);

		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mepSpinner.setAdapter(aa);
		
		imeiEditText.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				generateUnlockCode();
				return false;
			}
		});

	}

	class LinkOnClickListener implements View.OnClickListener {

		private String link;
		
		private LinkOnClickListener(String link) {
			this.link = link;
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
		    startActivity(intent);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Do nothing.
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		generateUnlockCode();
	}
	
	private void generateUnlockCode() {
		String imei = imeiEditText.getText().toString();
		String selectedMep = mepSpinner.getSelectedItem().toString();
		if (!selectedMep.equals("") && (imei.length() == 15)) {
			unlockCodeTextView.setText(bbDroidUnlocker.generateUnlockCode(imei, selectedMep));
		} else {
			unlockCodeTextView.setText("");
		}
	}
	
}