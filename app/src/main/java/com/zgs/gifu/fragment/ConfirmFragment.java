package com.zgs.gifu.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zgs.gifu.R;

/**
 * Created by zgs on 2017/1/2.
 */

public class ConfirmFragment extends DialogFragment {

    private String titleStr;
    private String negativeStr;
    private String positiveStr;
    private String confirmMsg;

    public static ConfirmFragment newInstance(String titleStr, String negativeStr,
                                              String positiveStr, String confirmMsg) {
        ConfirmFragment newFragment = new ConfirmFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", titleStr);
        bundle.putString("negativeStr", negativeStr);
        bundle.putString("positiveStr", positiveStr);
        bundle.putString("confirmMsg", confirmMsg);
        newFragment.setArguments(bundle);
        return newFragment;

    }

    public interface DialogFragmentClick {
        void doPositiveClick(int position);
        void doNegativeClick(int position);
    }

    private DialogFragmentClick mOnDialogItemClick;
    private int position;

    public void setmOnDialogItemClick(int position, DialogFragmentClick mOnDialogItemClick) {
        this.mOnDialogItemClick = mOnDialogItemClick;
        this.position = position;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.confirm_dialog, null);

        Bundle args = getArguments();
        if (args != null) {
            this.titleStr = args.getString("title");
            this.negativeStr = args.getString("negativeStr");
            this.positiveStr = args.getString("positiveStr");
            this.confirmMsg = args.getString("confirmMsg");
        }

        TextView mTextView = (TextView) view.findViewById(R.id.confirm_text);
        mTextView.setText(this.confirmMsg);
        builder.setView(view)
                .setTitle(titleStr)
                .setPositiveButton(positiveStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOnDialogItemClick.doPositiveClick(position);
                    }
                })
                .setNegativeButton(negativeStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOnDialogItemClick.doNegativeClick(position);
                    }
                });

        return builder.create();
    }
}
