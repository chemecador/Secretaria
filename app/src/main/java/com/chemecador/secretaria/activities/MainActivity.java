package com.chemecador.secretaria.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.databinding.ActivityMainBinding;
import com.chemecador.secretaria.db.DB;
import com.chemecador.secretaria.fragments.CalendarFragment;
import com.chemecador.secretaria.fragments.ListsFragment;
import com.chemecador.secretaria.ui.login.LoginActivity;
import com.chemecador.secretaria.utils.PreferencesHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    public static final String CALENDAR = "calendar";
    public static final String LIST = "list";

    private ActivityMainBinding binding;
    private CalendarFragment calendarFragment;
    private ListsFragment listsFragment;
    private SharedPreferences prefs;

    private String mode;


    private final BottomNavigationView.OnItemSelectedListener navItemSelectedListener =
            item -> {
                if (item.getItemId() == R.id.menu_lists) {
                    mode = LIST;
                    switchState();
                    return true;
                } else if (item.getItemId() == R.id.menu_calendar) {
                    mode = CALENDAR;
                    switchState();
                    return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar el estado inicial del switcher
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        calendarFragment = new CalendarFragment();
        listsFragment = new ListsFragment();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mode = prefs.getString("mode", "list");

        init();
    }

    private void init() {
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.bnv.setOnItemSelectedListener(navItemSelectedListener);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Configurar el clic del botón de retroceso
        binding.toolbar.setNavigationOnClickListener(view -> onBackPressed());

        switchState();
        binding.bnv.setSelectedItemId(mode.equals(CALENDAR) ? R.id.menu_calendar : R.id.menu_lists);

    }

    private void switchState() {
        Fragment fragment = null;
        switch (this.mode) {
            case CALENDAR:
                fragment = calendarFragment;
                break;
            case LIST:
                fragment = listsFragment;
                break;
        }
        prefs.edit().putString("mode", this.mode).apply();
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.add_friend) {
            startActivity(new Intent(this, FriendsActivity.class));
            return true;
        }  else if (id == R.id.about_us) {
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;
        } else if (id == R.id.logout) {
            if (PreferencesHandler.isOnline(this)){
                DB.getInstance()?.deleteAll()
            }
            this.finish();
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        MenuItem addFriendMenuItem = menu.findItem(R.id.add_friend);

        addFriendMenuItem.setEnabled(PreferencesHandler.isOnline(this));

        return true;
    }
}