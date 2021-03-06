package com.kistalk.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kistalk.android.R;
import com.kistalk.android.util.Constant;
import com.kistalk.android.util.KT_TransferManager;

public class LoginActivity extends Activity implements Constant {

	private Button loginButton;
	private Button scanQrButton;
	private EditText usernameField;
	private EditText tokenField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view_layout);
	}

	@Override
	protected void onStart() {
		super.onStart();
		loginButton = (Button) findViewById(R.id.login_button_login);
		scanQrButton = (Button) findViewById(R.id.login_button_scan_qr);
		usernameField = (EditText) findViewById(R.id.login_inputbox_username);
		tokenField = (EditText) findViewById(R.id.login_inputbox_token);

		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}
		});

		scanQrButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				try {
					startActivityForResult(intent, REQUEST_QR_READER);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(LoginActivity.this, "QR reader not found",
							Toast.LENGTH_LONG).show();
				}

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == REQUEST_QR_READER)
			if (resultCode == RESULT_OK) {
				String result = intent.getStringExtra("SCAN_RESULT");
				String[] credentials = result.split(":");
				if (credentials.length == 2) {
					usernameField.setText(credentials[0]);
					tokenField.setText(credentials[1]);
					login();
				} else
					Toast.makeText(LoginActivity.this, "Problem with QR code",
							Toast.LENGTH_LONG).show();
			}
	}

	private void login() {
		String username = usernameField.getText().toString().trim();
		String token = tokenField.getText().toString().trim();

		ProgressDialog pd = new ProgressDialog(LoginActivity.this);
		pd.setMessage("Validating credentials");
		pd.setTitle("Validating");
		pd.show();

		KT_TransferManager transferManager = new KT_TransferManager();
		boolean credentialsOk = transferManager.validate(username, token);

		pd.dismiss();

		if (credentialsOk) {
			Intent result = new Intent();
			result.putExtra(ARG_USERNAME, username);
			result.putExtra(ARG_TOKEN, token);
			setResult(RESULT_OK, result);
			finish();
		} else {
			Toast.makeText(LoginActivity.this, "Bad credentials",
					Toast.LENGTH_LONG).show();
		}

	}
}
