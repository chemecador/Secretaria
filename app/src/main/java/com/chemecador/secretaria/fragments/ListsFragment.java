package com.chemecador.secretaria.fragments;

import static com.chemecador.secretaria.utils.Utils.ERROR;
import static com.chemecador.secretaria.utils.Utils.WARNING;
import static com.chemecador.secretaria.utils.Utils.showToast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.chemecador.secretaria.adapters.ListAdapter;
import com.chemecador.secretaria.api.Client;
import com.chemecador.secretaria.api.Service;
import com.chemecador.secretaria.databinding.FragmentListsBinding;
import com.chemecador.secretaria.db.DB;
import com.chemecador.secretaria.gui.CustomToast;
import com.chemecador.secretaria.items.NotesList;
import com.chemecador.secretaria.logger.Logger;
import com.chemecador.secretaria.responses.IdResponse;
import com.chemecador.secretaria.ui.login.LoginActivity;
import com.chemecador.secretaria.utils.PreferencesHandler;
import com.chemecador.secretaria.utils.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ListsFragment extends Fragment {

    private static final String TAG = ListsFragment.class.getSimpleName();
    private FragmentListsBinding binding;
    private Context ctx;
    private RecyclerView rvLists;
    private ListAdapter adapter;
    private List<NotesList> lists;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListsBinding.inflate(inflater, container, false);

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        Button btnDay = toolbar.findViewById(R.id.btn_day);
        btnDay.setVisibility(View.GONE);


        init();

        return binding.getRoot();

    }

    private void init() {
        lists = DB.getInstance(ctx).getLists();
        rvLists = binding.getRoot().findViewById(R.id.rv);
        rvLists.setLayoutManager(new LinearLayoutManager(ctx));
        adapter = new ListAdapter(ctx, lists);
        adapter.setOnLongClickListener(adapter);
        rvLists.setAdapter(adapter);

        // Obtener una referencia al ActionBar
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        binding.fab.setOnClickListener(view -> createList());
    }

    public void createList() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx);

        // Inflar la vista personalizada
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View dialogView = inflater.inflate(R.layout.dialog_new_list, null);
        builder.setView(dialogView);
        SwitchMaterial switchPublic = dialogView.findViewById(R.id.switch_public);
        MaterialCheckBox cbCheck = dialogView.findViewById(R.id.cb_check_list);
        ImageView ivInfo = dialogView.findViewById(R.id.iv_info);
        if (PreferencesHandler.isOnline(ctx)) {
            ivInfo.setVisibility(View.VISIBLE);
            switchPublic.setVisibility(View.VISIBLE);
        } else {
            ivInfo.setVisibility(View.GONE);
            switchPublic.setVisibility(View.GONE);
        }
        TextInputLayout textInputLayout = dialogView.findViewById(R.id.til_list_name);
        AlertDialog dialog = builder.show();

        ivInfo.setOnClickListener((v) -> {
            showInfo();
        });

        // Obtener los botones del diálogo después de mostrarlo para poder configurar sus acciones
        Button positiveButton = dialogView.findViewById(R.id.btn_ok);
        positiveButton.setOnClickListener(v -> {
            if (textInputLayout.getEditText() == null) return;
            String listName = textInputLayout.getEditText().getText().toString();
            if (TextUtils.isEmpty(listName) || listName.trim().equals("")) {
                // El campo está vacío, mostrar un Snackbar con el mensaje de error
                textInputLayout.setError(getString(R.string.error_empty_field));
            } else {
                textInputLayout.setError(null);
                DB db = DB.getInstance(ctx);
                if (!db.existsList(listName)) {
                    // si es offline, es privada por defecto. Si es online, hay que mirar el switch.
                    int isPublic = PreferencesHandler.isOnline(ctx) ? NotesList.PUBLIC : NotesList.PRIVATE;
                    if (isPublic == NotesList.PUBLIC) isPublic = switchPublic.isChecked() ? NotesList.PUBLIC : NotesList.PRIVATE;
                    NotesList mList = new NotesList(listName,
                            isPublic,
                            cbCheck.isChecked() ? NotesList.CHECK_LIST : NotesList.NORMAL_LIST);
                    if (PreferencesHandler.isOnline(ctx)) {
                        syncList(mList);
                    } else {
                        insertList(mList);
                    }
                    dialog.dismiss();
                } else {
                    Utils.showToast(ctx, WARNING, getString(R.string.list_already_exists));
                }
            }
        });

        Button negativeButton = dialogView.findViewById(R.id.btn_cancel);
        negativeButton.setOnClickListener(v -> {
            // Acción al pulsar el botón negativo
            dialog.dismiss();
        });
    }

    private void showInfo() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx);

        // Inflar la vista personalizada
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View dialogView = inflater.inflate(R.layout.dialog_list_info, null);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();

    }

    private void insertList(NotesList mList) {
        int listId = DB.getInstance(ctx).insertList(mList);
        if (!PreferencesHandler.isOnline(ctx)) mList.setId(listId);
        lists.add(mList);
        adapter.notifyItemInserted(lists.size() - 1);
        Snackbar.make(binding.getRoot(), getString(R.string.create_list_success), Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .show();
        Logger.i(TAG, "Lista insertada correctamente: " + mList);
    }


    private void syncList(NotesList mList) {

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
        // Utilizar el servicio para realizar llamadas a la API
        Call<IdResponse> call = apiService.createList(token, userId, mList);

        // Ejecutar la llamada de forma asíncrona
        call.enqueue(new Callback<IdResponse>() {
            @Override
            public void onResponse(@NonNull Call<IdResponse> call, @NonNull Response<IdResponse> response) {
                if (response.isSuccessful()) {

                    // Procesar la respuesta exitosa
                    IdResponse result = response.body();
                    if (result == null) return;
                    int id = result.getId();
                    mList.setId(id);
                    insertList(mList);

                } else if (response.code() == 401) {
                    // Manejar el error de respuesta
                    showToast(ctx, ERROR, response.code() + " : " + getString(R.string.unauthorized));
                } else {
                    showToast(ctx, ERROR, response.code() + " : " + getString(R.string.server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<IdResponse> call, @NonNull Throwable t) {


                // Manejar el error de conexión o la excepción
                showToast(ctx, ERROR, getString(R.string.server_error));

            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.ctx = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}