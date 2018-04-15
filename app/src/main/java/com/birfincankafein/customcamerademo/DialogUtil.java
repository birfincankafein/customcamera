package com.birfincankafein.customcamerademo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by M_TOKSOY2 on 15.03.2018.
 */

public class DialogUtil {
    public static AlertDialog createProgressDialog(Context context, String title, String message){
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        ((TextView) dialogView.findViewById(R.id.textView_progressmessage)).setText(message);
        return new AlertDialog.Builder(context)
                .setView(dialogView)
                .setTitle(title)
                .setCancelable(false)
                .create();
    }

    public static AlertDialog createInputDialog(Context context, String title, final HashMap<String, String> hintsDefaults, @Nullable final onDialogEventListener listener){
        LinearLayout dialogView = new LinearLayout(context);
        int dimenPadding = (int) context.getResources().getDimension(R.dimen.dialog_padding);
        dialogView.setPadding(dimenPadding, dimenPadding, dimenPadding, dimenPadding/2);
        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final HashMap<String, EditText> hintToView = new HashMap<>();

        for(String hint : hintsDefaults.keySet()){
            TextInputLayout textInputLayout = new TextInputLayout(context);
            EditText editText = new EditText(context);
            editText.setHint(hint);
            editText.setText(hintsDefaults.get(hint));
            textInputLayout.addView(editText, new TextInputLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            dialogView.addView(textInputLayout, layoutParams);
            hintToView.put(hint, editText);
        }

        AlertDialog.Builder dialog =  new AlertDialog.Builder(context)
                .setView(dialogView)
                .setTitle(title)
                .setCancelable(false);

        if(listener != null){
            dialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, String> values = new HashMap<>();
                        for(String hint : hintToView.keySet()){
                            String value = hintToView.get(hint).getText().toString();
                            values.put(hint, value);
                        }
                        listener.onDialogEvent(true, values);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogEvent(false, null);
                    }
                });
        }
        return dialog.create();
    }

    public interface onDialogEventListener{
        void onDialogEvent(boolean isPositiveButton, @Nullable HashMap<String, String> values);
    }
}
