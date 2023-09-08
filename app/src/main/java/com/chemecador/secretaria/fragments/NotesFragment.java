package com.chemecador.secretaria.fragments;

import static com.chemecador.secretaria.utils.Utils.ERROR;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.adapters.NoteAdapter;
import com.chemecador.secretaria.api.Client;
import com.chemecador.secretaria.api.Service;
import com.chemecador.secretaria.databinding.FragmentNotesBinding;
import com.chemecador.secretaria.db.DB;
import com.chemecador.secretaria.gui.CustomToast;
import com.chemecador.secretaria.items.Note;
import com.chemecador.secretaria.items.NotesList;
import com.chemecador.secretaria.logger.Logger;
import com.chemecador.secretaria.requests.NoteRequest;
import com.chemecador.secretaria.responses.IdResponse;
import com.chemecador.secretaria.ui.login.LoginActivity;
import com.chemecador.secretaria.utils.PreferencesHandler;
import com.chemecador.secretaria.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NotesFragment extends Fragment {

    public static final String TAG = NotesFragment.class.getSimpleName();
    private List<Note> notes;
    private Context ctx;
    private NoteAdapter adapter;
    private int listId;
    private FragmentNotesBinding binding;
    private boolean isPublic;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotesBinding.inflate(inflater, container, false);

        if (getArguments() != null) listId = getArguments().getInt("listId");

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        Button btnDay = toolbar.findViewById(R.id.btn_day);
        btnDay.setVisibility(View.GONE);

        init();

        return binding.getRoot();

    }

    private void init() {
        notes = DB.getInstance(ctx).getNotesByList(listId);
        isPublic = DB.getInstance(ctx).getPrivacy(listId) == NotesList.PUBLIC;
        RecyclerView rvLists = binding.getRoot().findViewById(R.id.rv);
        adapter = new NoteAdapter(ctx, notes, isPublic);
        rvLists.setLayoutManager(new LinearLayoutManager(ctx));
        rvLists.setAdapter(adapter);


        // Obtener una referencia al ActionBar
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = binding.getRoot().findViewById(R.id.fab);
        fab.setOnClickListener(view -> createNote());
    }

    private void createNote() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx);
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View dialogView = inflater.inflate(R.layout.dialog_new_note, null);
        MaterialCheckBox cbContent = dialogView.findViewById(R.id.cb_content);


        TextInputLayout tilTitle = dialogView.findViewById(R.id.til_title);
        TextInputLayout tilContent = dialogView.findViewById(R.id.til_content);


        cbContent.addOnCheckedStateChangedListener((checkBox, state) -> {
            if (state == 1) {
                tilContent.setVisibility(View.VISIBLE);
                dialogView.findViewById(R.id.tv_content).setVisibility(View.VISIBLE);
            } else {
                tilContent.setVisibility(View.GONE);
                dialogView.findViewById(R.id.tv_content).setVisibility(View.GONE);
            }
        });
        Button positiveButton = dialogView.findViewById(R.id.btn_ok);
        Button negativeButton = dialogView.findViewById(R.id.btn_cancel);
        builder.setView(dialogView);

        AlertDialog dialog = builder.show();

        positiveButton.setOnClickListener(v -> {
            Note note = new Note();
            note.setTitle(tilTitle.getEditText().getText().toString());
            note.setListId(listId);
            note.setContent(tilContent.getVisibility() == View.VISIBLE ? tilContent.getEditText().getText().toString() : "");
            note.setStatus(DB.getInstance(ctx).getType(listId));

            if (PreferencesHandler.isOnline(ctx)) {
                syncNote(note);
            } else {
                insertNote(note);
            }

            dialog.dismiss();
        });
        negativeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void syncNote(Note mNote) {

        // Obtener la instancia de Retrofit
        Retrofit retrofit = Client.getClient();

        // Crear una instancia del servicio de la API
        Service apiService = retrofit.create(Service.class);

        int userId = PreferenceManager.getDefaultSharedPreferences(ctx).getInt("id", -1);
        String token = PreferenceManager.getDefaultSharedPreferences(ctx).getString("token", "");
        if (userId == -1) {
            new CustomToast(ctx, ERROR, Toast.LENGTH_LONG).show(getString(R.string.login_again));
            ((Activity) ctx).finish();
            startActivity(new Intent(ctx, LoginActivity.class));
            return;
        }

        NoteRequest nr = new NoteRequest(mNote);
        // Utilizar el servicio para realizar llamadas a la API
        Call<IdResponse> call = apiService.createNote(token, userId, mNote.getListId(), nr);

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(new Callback<IdResponse>() {
            @Override
            public void onResponse(@NonNull Call<IdResponse> call, @NonNull Response<IdResponse> response) {
                if (response.isSuccessful()) {

                    // Procesar la respuesta exitosa
                    IdResponse result = response.body();
                    assert result != null;
                    int id = result.getId();
                    mNote.setId(id);
                    insertNote(mNote);
                } else if (response.code() == 401) {
                    // Manejar el error de respuesta
                    Utils.showToast(ctx, ERROR, response.code() + " : " + getString(R.string.unauthorized));
                } else {
                    Utils.showToast(ctx, ERROR, response.code() + " : " + getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<IdResponse> call, @NonNull Throwable t) {

                // Manejar el error de conexión o la excepción
                Utils.showToast(ctx, ERROR, getString(R.string.server_error) + " :\n" + t.getMessage());

            }
        });
    }

    private void insertNote(Note mNote) {
        DB.getInstance(ctx).insertNote(mNote);
        notes.add(mNote);
        // Notificar al adaptador del cambio en la lista de tareas
        adapter.notifyItemInserted(notes.size() - 1);
        Snackbar.make(binding.getRoot(), getString(R.string.create_note_success), Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .show();
        Logger.i(TAG, "Nota insertada correctamente: " + mNote);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.ctx = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Restaurar la visibilidad del botón de retroceso al salir del fragmento
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}