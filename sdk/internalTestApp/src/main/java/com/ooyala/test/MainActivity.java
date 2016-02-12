package com.ooyala.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * This is the opening activity for the app.
 *
 */
public class MainActivity extends Activity implements OnItemClickListener {
  ArrayAdapter<String> mainListAdapter = null;
  final String TAG = this.getClass().toString();
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity_layout);

    //Create the adapter, get the activity list from the Applictation "global" context.
    mainListAdapter = new ArrayAdapter<String>(this, R.layout.main_list_item);
    mainListAdapter.addAll(InternalTestApplication.getActivityMap().keySet());
    mainListAdapter.notifyDataSetChanged();

    ListView mainListView = (ListView) findViewById(R.id.mainActivityListView);
    mainListView.setAdapter(mainListAdapter);

    mainListView.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> l, View v, int pos, long id) {
    Class<? extends Activity> selectedClass = InternalTestApplication.getActivityMap().get(mainListAdapter.getItem(pos));

    Intent intent = new Intent(this, selectedClass);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    startActivity(intent);
    return;
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    for(String key : InternalTestApplication.getActivityMap().keySet()) {
      MenuItem item = menu.add(key);
      item.setTitleCondensed(InternalTestApplication.getCondensedActivityNameMap().get(key));
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Class<? extends Activity> selectedClass = InternalTestApplication.getActivityMap().get(item.getTitle().toString());

    if(selectedClass.equals(this.getClass())){
      Log.d(TAG, "Selected currently showing activity");
      return true;
    }

    Intent intent = new Intent(this, selectedClass);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    startActivity(intent);
    return true;
  }
}
