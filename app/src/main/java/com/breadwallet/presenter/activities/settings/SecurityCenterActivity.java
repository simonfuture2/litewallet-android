package com.breadwallet.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.ActivityUTILS;
import com.breadwallet.presenter.activities.intro.WriteDownActivity;
import com.breadwallet.presenter.activities.UpdatePitActivity;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.presenter.entities.BRSecurityCenterItem;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.manager.SharedPreferencesManager;
import com.breadwallet.tools.security.KeyStoreManager;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.platform.HTTPServer.URL_SUPPORT;

public class SecurityCenterActivity extends BRActivity {
    private static final String TAG = SecurityCenterActivity.class.getName();

    public ListView mListView;
    public RelativeLayout layout;
    public List<BRSecurityCenterItem> itemList;
    public static boolean appVisible = false;
    private static SecurityCenterActivity app;
    private ImageButton close;

    public static SecurityCenterActivity getApp() {
        return app;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_center);

        itemList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.menu_listview);
        mListView.addFooterView(new View(this), null, true);
        mListView.addHeaderView(new View(this), null, true);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setClickable(false);
        close = (ImageButton) findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        updateList();


        ImageButton faq = (ImageButton) findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity app = SecurityCenterActivity.this;
                Intent intent = new Intent(app, WebViewActivity.class);
                intent.putExtra("url", URL_SUPPORT);
                intent.putExtra("articleId", BRConstants.securityCenter);
                app.startActivity(intent);
                app.overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
        appVisible = true;
        app = this;
        ActivityUTILS.init(this);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public RelativeLayout getMainLayout() {
        return layout;
    }

    public class SecurityCenterListAdapter extends ArrayAdapter<BRSecurityCenterItem> {

        private List<BRSecurityCenterItem> items;
        private Context mContext;
        private int defaultLayoutResource = R.layout.security_center_list_item;

        public SecurityCenterListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<BRSecurityCenterItem> items) {
            super(context, resource);
            this.items = items;
            this.mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

//            Log.e(TAG, "getView: pos: " + position + ", item: " + items.get(position));
            if (convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(defaultLayoutResource, parent, false);
            }
            TextView title = (TextView) convertView.findViewById(R.id.item_title);
            TextView text = (TextView) convertView.findViewById(R.id.item_text);
            ImageView checkMark = (ImageView) convertView.findViewById(R.id.check_mark);

            title.setText(items.get(position).title);
            text.setText(items.get(position).text);
            checkMark.setImageResource(items.get(position).checkMarkResId);
            convertView.setOnClickListener(items.get(position).listener);
            return convertView;

        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        appVisible = false;
    }

    private void updateList() {
        boolean isPinSet = KeyStoreManager.getPinCode(this).length() == 6;
        itemList.clear();
        itemList.add(new BRSecurityCenterItem("6-Digit PIN", "Unlocks your Bread, authorizes send money.",
                isPinSet ? R.drawable.ic_check_mark_blue : R.drawable.ic_check_mark_grey, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 6-Digit PIN");
                Intent intent = new Intent(SecurityCenterActivity.this, UpdatePitActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }));

        int resId = Utils.isFingerprintEnrolled(SecurityCenterActivity.this)
                && SharedPreferencesManager.getUseFingerprint(SecurityCenterActivity.this)
                ? R.drawable.ic_check_mark_blue
                : R.drawable.ic_check_mark_grey;

        if (Utils.isFingerprintAvailable(this)) {
            itemList.add(new BRSecurityCenterItem("FingerPrint", "Unlocks your Bread, authorizes send money to set limit.",
                    resId, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Touch ID");
                    Intent intent = new Intent(SecurityCenterActivity.this, FingerprintActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                }
            }));
        }

        boolean isPaperKeySet = SharedPreferencesManager.getPhraseWroteDown(this);
        itemList.add(new BRSecurityCenterItem("Paper Key", "Restores your Bread on new devices and after software updates.",
                isPaperKeySet ? R.drawable.ic_check_mark_blue : R.drawable.ic_check_mark_grey, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Paper Key");
                Intent intent = new Intent(SecurityCenterActivity.this, WriteDownActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.fade_down);
            }
        }));

        mListView.setAdapter(new SecurityCenterListAdapter(this, R.layout.menu_list_item, itemList));
    }
}
