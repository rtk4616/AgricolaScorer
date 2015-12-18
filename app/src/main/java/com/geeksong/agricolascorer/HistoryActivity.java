package com.geeksong.agricolascorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.geeksong.agricolascorer.control.DatePickerFragment;
import com.geeksong.agricolascorer.listadapter.GameHistoryAdapter;
import com.geeksong.agricolascorer.mapper.GameHistoryMapper;
import com.geeksong.agricolascorer.mapper.GameSerializationMapper;
import com.geeksong.agricolascorer.model.Game;

import java.io.IOException;
import java.util.Calendar;

public class HistoryActivity extends Activity {
	private static final int MENU_VIEW_GAME = 0;
	private static final int MENU_DELETE_GAME = 1;
	private static final int MENU_EDIT_GAME = 2;
	private static final int MENU_EDIT_GAME_DATE = 3;
	private static final int MENU_SHARE_GAME = 4;
	
	private static final int RESCORE_ACTIVITY = 0;
	
	private GameHistoryMapper historyMapper;
	private GameHistoryAdapter historyAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        historyMapper = new GameHistoryMapper(this);
        historyAdapter = historyMapper.getListAdapter();
        
        checkNoHistoryLabelVisibility();

        ExpandableListView list = (ExpandableListView) this.findViewById(R.id.historyList);
        list.setAdapter(historyAdapter);
        
        registerForContextMenu(list);
    }
    
    private void checkNoHistoryLabelVisibility() {
    	if(!historyAdapter.isEmpty())
    		return;
    	
    	TextView noHistoryLabel = (TextView) this.findViewById(R.id.noHistoryLabel);
    	noHistoryLabel.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	menu.add(Menu.NONE, MENU_VIEW_GAME, Menu.NONE, R.string.view_game);
    	menu.add(Menu.NONE, MENU_DELETE_GAME, Menu.NONE, R.string.delete_game);
    	menu.add(Menu.NONE, MENU_EDIT_GAME, Menu.NONE, R.string.edit_game);
    	menu.add(Menu.NONE, MENU_EDIT_GAME_DATE, Menu.NONE, R.string.edit_game_date);
    	//menu.add(Menu.NONE, MENU_SHARE_GAME, Menu.NONE, R.string.share_game);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
    	int position = (int) info.id;
    	final Game game = (Game) historyAdapter.getGroup(position);
    	
    	switch(item.getItemId()) {
    		case MENU_VIEW_GAME:
                GameCache.getInstance().setGame(game);
                Intent rescore = new Intent(this, FinishedActivity.class);
                startActivityForResult(rescore, RESCORE_ACTIVITY);
    			return true;
    		case MENU_DELETE_GAME:
    			historyMapper.deleteGame(game);
    			historyAdapter.deleteGame(game);
    			checkNoHistoryLabelVisibility();
    			return true;
    		case MENU_EDIT_GAME:
    			// Some games are entered manually in the database, with only a final score. We can't edit those here.
    			if(game.hasScoresWithOnlyTotalScore()) {
    				new AlertDialog.Builder(this)
    					.setTitle(R.string.can_not_edit)
    					.setMessage(R.string.no_detail_scores)
    					.setPositiveButton(R.string.ok, null)
    					.show();
    				return true;
    			}
    			
    			GameCache.getInstance().setGame(game);
    			rescore = new Intent(this, ScorePlayersActivity.class);
    			startActivityForResult(rescore, RESCORE_ACTIVITY);
    			
    			return true;
    		case MENU_EDIT_GAME_DATE:
    			DatePickerFragment datePickerFragment = new DatePickerFragment();
    			datePickerFragment.setDefault(game.getDate());
    			datePickerFragment.setOnDateSetListener(new OnDateSetListener() {
    				public void onDateSet(DatePicker datePicker, int year, int month, int day) {
    					Calendar cal = Calendar.getInstance();
    					cal.set(year, month, day);
    					game.setDate(cal.getTime());

    					historyMapper.updateGameDate(game);
    					historyAdapter.notifyDataSetChanged();
    				}
    			});
    			datePickerFragment.show(getFragmentManager(), "datePicker");
    			
    			return true;
    			
    		case MENU_SHARE_GAME:
    			try {
	    			Intent shareIntent = new Intent();
	    			shareIntent.setAction(Intent.ACTION_SEND);
	    			shareIntent.putExtra(Intent.EXTRA_STREAM, GameSerializationMapper.serialize(game));
	    			shareIntent.setType("*/*");
	    			startActivity(shareIntent);
	    			return true;
    			} catch(IOException e) {
					// TODO: dialog here for error/log it
    			}
    			
    		default:
    	    	return super.onContextItemSelected(item);
    	}
    }
    
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
    	super.onActivityResult(reqCode, resultCode, data);
		
    	switch (reqCode) {
    	// If we come back here from editing, clear the game
    		case RESCORE_ACTIVITY:
    			GameCache.getInstance().clearGame();    			
    			
    			break;
    	}
	}
    
    public void expandCollapseAll(View source) {
    	Button expandCollapseAll = (Button) source;
    	String text = expandCollapseAll.getText().toString();
    	ExpandableListView list = (ExpandableListView) this.findViewById(R.id.historyList);
    	String expandAll = getResources().getString(R.string.expand_all);
    	String collapseAll = getResources().getString(R.string.collapse_all);
		boolean expanding = text.equals(expandAll);

		for(int i = 0; i < historyAdapter.getGroupCount(); i++) {
    		if(expanding)
	    		list.expandGroup(i);
    		else
	    		list.collapseGroup(i);
    	}
    			
	    expandCollapseAll.setText(expanding ? collapseAll : expandAll);
    }
}
