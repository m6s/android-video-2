package info.mschmitt.support.databinding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.widget.TextView;

/**
 * @author Matthias Schmitt
 */
public class TextViewBindingAdapter {
    @BindingAdapter("intText")
    public static void setIntText(TextView textView, int intText) {
        String text = Integer.toString(intText);
        CharSequence charSequence = textView.getText();
        String string = charSequence.toString();
        if (string.isEmpty()) {
            textView.setText(text);
            return;
        }
        int oldIntText = Integer.parseInt(string);
        if (intText != oldIntText) {
            textView.setText(text);
        }
    }

    @InverseBindingAdapter(attribute = "intText", event = "android:textAttrChanged")
    public static int getIntText(TextView textView) {
        String string = textView.getText().toString();
        return string.isEmpty() ? 0 : Integer.parseInt(string);
    }

    @BindingAdapter("longText")
    public static void setLongText(TextView textView, long longText) {
        String text = Long.toString(longText);
        CharSequence charSequence = textView.getText();
        String string = charSequence.toString();
        if (string.isEmpty()) {
            textView.setText(text);
            return;
        }
        int oldIntText = Integer.parseInt(string);
        if (longText != oldIntText) {
            textView.setText(text);
        }
    }

    @InverseBindingAdapter(attribute = "longText", event = "android:textAttrChanged")
    public static long getLongText(TextView textView) {
        String string = textView.getText().toString();
        return string.isEmpty() ? 0 : Long.parseLong(string);
    }

    @BindingAdapter("doubleText")
    public static void setDoubleText(TextView textView, double doubleText) {
        String text = Double.toString(doubleText);
        CharSequence charSequence = textView.getText();
        String string = charSequence.toString();
        if (string.isEmpty()) {
            textView.setText(text);
            return;
        }
        double oldDoubleText = Double.parseDouble(string);
        if (doubleText != oldDoubleText) {
            textView.setText(text);
        }
    }

    @InverseBindingAdapter(attribute = "doubleText", event = "android:textAttrChanged")
    public static double getDoubleText(TextView textView) {
        String string = textView.getText().toString();
        return string.isEmpty() ? 0 : Double.parseDouble(string);
    }
}
