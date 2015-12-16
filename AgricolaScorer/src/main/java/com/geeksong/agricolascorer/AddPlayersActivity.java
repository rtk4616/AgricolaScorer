package com.geeksong.agricolascorer;

import com.geeksong.agricolascorer.mapper.PlayerMapper;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class AddPlayersActivity extends Activity implements OnItemClickListener {
	private static final int PICK_CONTACT = 1;
	private static final int PICK_DATABASE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_players);
        
        ActionBarHelper.setActionBarTitle(this, R.string.add_player);
        
        ListView list = (ListView) this.findViewById(R.id.recentPlayersList);
        list.setOnItemClickListener(this);
        list.setAdapter(PlayerMapper.getInstance().getTopPlayersListAdapter());
	}
              
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TextView item = (TextView) view.findViewById(R.id.name);
		
		putNameIntoInput(item.getText().toString().trim());
		addPlayerToGame(null);
	}
    
    private void putNameIntoInput(String name) {
    	EditText inputPlayerName = (EditText) findViewById(R.id.inputPlayerName);
    	inputPlayerName.setText(name);	
    }
    
    private String getInputName() {
    	EditText inputPlayerName = (EditText) findViewById(R.id.inputPlayerName);
    	return inputPlayerName.getText().toString();
    }
     
    public void addPlayerToGame(View source) {
    	String playerName = getInputName();
    	returnToCreateGame(playerName);
    }
    
    private void returnToCreateGame(String playerName) {
    	Intent backToCreateGame = new Intent();
    	backToCreateGame.putExtra(CreateGameActivity.AddedPlayerBundleKey, playerName);
    	setResult(RESULT_OK, backToCreateGame);
    	finish();
    }
    
    public void searchAddressBook(View source) {
    	Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    	startActivityForResult(intent, PICK_CONTACT);
    }
    
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
    	super.onActivityResult(reqCode, resultCode, data);
			
    	switch (reqCode) {
    		case PICK_CONTACT:
    			if (resultCode != Activity.RESULT_OK)
    				return;

    			Uri contactData = data.getData();
				Cursor c = getContentResolver().query(contactData, null, null, null, null);

				if (c.moveToFirst()) {
					String playerName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					returnToCreateGame(playerName);  
				}
    			break;
    		case PICK_DATABASE:
    			if(resultCode == Activity.RESULT_CANCELED)
    				return;
    			
    			String playerName = data.getStringExtra(CreateGameActivity.AddedPlayerBundleKey);
    			returnToCreateGame(playerName);
    			break;    			
    	}
	}
        
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_players, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    		case R.id.fromPlayerList:
    			Intent intent = new Intent(this, PlayerListActivity.class);
    			startActivityForResult(intent, PICK_DATABASE);
    			break;
    	}
        return true;
    }
}
