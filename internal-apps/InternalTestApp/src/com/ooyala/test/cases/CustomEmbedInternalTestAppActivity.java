package com.ooyala.test.cases;

import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.test.BaseInternalTestAppActivity;
import com.ooyala.test.R;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.inputmethod.InputMethodManager;

public class CustomEmbedInternalTestAppActivity extends BaseInternalTestAppActivity {
//  From the BaseInternalTestAppActivity
//  OptimizedOoyalaPlayerLayoutController playerLayoutController;
//  OoyalaPlayer player;

  final String TAG = this.getClass().toString();
  final String DOMAIN = "ooyala.com";

  private OoyalaPlayerLayout playerLayout;
  private EditText pcodeText;
  private EditText embedCodeText;
  private Button setButton;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.custom_embed_code_case_layout);

    pcodeText = (EditText) findViewById(R.id.pcodeText);
    pcodeText.setText("f34784cb010846369c31af0bdd0ec83e");

    embedCodeText = (EditText) findViewById(R.id.embedCodeText);
    embedCodeText.setText("Rjcmp0ZDr5yFbZPEfLZKUveR_2JzZjMO");

    setButton = (Button) findViewById(R.id.setButton);
    setButton.setOnClickListener(this);

    //Initialize the player
    playerLayout = (OoyalaPlayerLayout) findViewById(R.id.customOoyalaPlayer);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.setButton) {
      //Hide keyboard
      InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

      //Suspend player if it exists
      if (playerLayoutController.getPlayer() != null) {
        playerLayoutController.getPlayer().suspend();
      }

      //Set the pcode and embed code
      Log.i(TAG, "Pcode: " + pcodeText.getText().toString() + "" + "Embed Code: " + embedCodeText.getText().toString());
      playerLayoutController = new OptimizedOoyalaPlayerLayoutController(playerLayout, pcodeText.getText().toString(), DOMAIN);
      player = playerLayoutController.getPlayer();
      player.setEmbedCode(embedCodeText.getText().toString());
    }
  }
}