package com.zgs.gifu.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zgs.gifu.R;
import com.zgs.gifu.constant.AppConstants;

public class TextFragment extends DialogFragment {
	private EditText mEditText;
	private int mMode;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		if (!(activity instanceof EditCallback)) {
			throw new IllegalStateException("EditCallback must be implemented");
		}
		super.onAttach(activity);
	}

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Light_NoTitleBar);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_text, container, false);

		mEditText = (EditText) view.findViewById(R.id.edit);

		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((actionId == EditorInfo.IME_ACTION_SEARCH) ||
						(actionId == EditorInfo.IME_ACTION_DONE) ||
						((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getKeyCode() == 66))) {
					done();
					return true;
				}
				return false;
			}
		});

		Button mOkBtn = (Button) view.findViewById(R.id.edit_ok);
		mOkBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				done();
			}
			
		});
		Button mCancelBtn = (Button) view.findViewById(R.id.edit_cancel);
		mCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		mEditText.requestFocus();
		Window dialog= getDialog().getWindow();
		if (dialog != null) {
			dialog.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}
		return view;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (getArguments() != null) {
			String args = getArguments().getString("textStr");
			mEditText.setText(args);
			mMode = AppConstants.TAG_MODIFY;
			if (args != null) {
				mEditText.setSelection(args.length());
			}
		} else {
			mEditText.setText("");
			mMode = AppConstants.TAG_CREATE;
		}
	}

	public void done() {
		Editable editable = this.mEditText.getText();
		if (editable != null) {
			String text = editable.toString();
			if (!TextUtils.isEmpty(text)) {
				EditCallback callback = (EditCallback) getActivity();
				callback.updateTextStr(mMode, text);
				dismiss();
			}
		}
	}

	public interface EditCallback {
		/**
		 * Update TextTag's text string.
		 * @param mode 0: create a new texttag, 1: modify an exist one
		 * @param text text string to be updated
		 * @return void
		 */
		void updateTextStr(int mode, String text);
	}
}
