package com.chemecador.secretaria.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.chemecador.secretaria.R;
import com.chemecador.secretaria.gui.CustomToast;
import com.chemecador.secretaria.interfaces.OnBackPressed;
import com.chemecador.secretaria.logger.Logger;
import com.chemecador.secretaria.provider.SecretariaFileProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ExplorerFragment extends Fragment implements OnBackPressed {

    private static final String TAG = ExplorerFragment.class.getSimpleName();

    private File currentDirectory;
    private File rootDirectory;

    private final List<AliasedFile> history;

    private ListView listView;
    private FileAdapter adapter;

    private Button buttonEnviar;
    private Button buttonBorrar;

    public ExplorerFragment() {
        // Required empty public constructor
        history = new ArrayList<>();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (currentDirectory == null) currentDirectory = context.getFilesDir();
        rootDirectory = context.getFilesDir();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = view.findViewById(R.id.explorer_listView);
        adapter = new FileAdapter(requireContext());

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this::fileSelected);

        buttonBorrar = view.findViewById(R.id.explorer_delete);
        buttonEnviar = view.findViewById(R.id.explorer_send);


        buttonBorrar.setOnClickListener((v) -> {
            boolean couldDeleteAll = true;
            for (AliasedFile file : adapter.getSelectedFiles()) {
                //Borrar solo si es un log
                if ((file.file.getAbsolutePath().contains("/logs/") && file.file.getName().endsWith(".txt")) ||
                        file.file.getAbsolutePath().contains("/csv/")) {
                    if (Logger.getSingleton().getFile().equals(file.file)) {
                        couldDeleteAll = false;
                    } else {
                        if (!file.file.delete()) couldDeleteAll = false;
                    }
                } else {
                    Logger.w(TAG, "Se intentó borrar el fichero " + file.file + " pero no se reconoció como fichero que el usuario deberia poder borrar");
                    couldDeleteAll = false;
                }
            }

            if (!couldDeleteAll) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error")
                        .setMessage("No se pudieron borrar todos los ficheros")
                        .create().show();
            }

            renderFiles();
        });

        buttonEnviar.setOnClickListener((v) -> {
            ArrayList<Uri> files = new ArrayList<>();
            for (AliasedFile file : adapter.getSelectedFiles()) {
                files.add(SecretariaFileProvider.getUriForFile(requireContext(), SecretariaFileProvider.authority, file.file));
            }

            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE)
                    .setType("*/*")
                    .putExtra(Intent.EXTRA_STREAM, files);

            if (currentDirectory.getAbsolutePath().endsWith("/logs")) {
                //TODO cambiar dirección de correo
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chemecador@gmail.com"});
            }

            startActivity(intent);
        });

        renderFiles();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explorer, container, false);
    }

    @Override
    public boolean onBackPressed() {
        if (history.size() == 0) {
            return false;
        } else {
            history.remove(history.size() - 1);
            if (history.size() > 0) {
                currentDirectory = history.get(history.size() - 1).file;
            } else {
                currentDirectory = rootDirectory;
            }
            renderFiles();
            return true;
        }
    }

    /**
     * Se ejecuta al seleccionar un fichero<br />
     * <p>
     * Si quieres añadir un fichero para que sea visible ten en cuenta que:<br/>
     * · Tienes que añadirlo en <code>exported_files.xml</code><br/>
     * · Tienes que hacer su directorio accesible añadiendo un item en <code>renderFiles()</code>
     */
    private void fileSelected(AdapterView<?> adapterView, View view, int position, long id) {
        //Al entrar en un directorio, cambiar a ese
        FileAdapter adapter = (FileAdapter) listView.getAdapter();
        AliasedFile alias = adapter.getItem(position);
        assert alias != null;
        File file = alias.file;

        if (file.isDirectory()) {
            if (Objects.requireNonNull(file.listFiles()).length == 0) {
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Aviso")
                        .setMessage("No se han encontrado archivos")
                        .create();
                dialog.show();
            } else {
                history.add(alias);
                currentDirectory = file;
            }
        } else {
            if (!file.exists()) {
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Aviso")
                        .setTitle("No se han encontrado archivos")
                        .create();
                dialog.show();
            } else {
                if (file.getName().toLowerCase().endsWith(".txt")) {
                    openFileIntent(Intent.ACTION_VIEW, SecretariaFileProvider.getUriForFile(requireContext(), file), "text/plain");
                } else if (file.getName().toLowerCase().endsWith(".csv")) {
                    //openFileIntent(Intent.ACTION_VIEW, SecretariaFileProvider.getUriForFile(requireContext(), file), "text/csv");
                    Intent target = new Intent(Intent.ACTION_VIEW);
                    Uri uri = SecretariaFileProvider.getUriForFile(requireContext(), file);
                    target.setDataAndType(uri, "text/csv");
                    target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent intent = Intent.createChooser(target, "Abrir documento csv con...");
                    try {
                        requireContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Instruct the user to install a PDF reader here, or something
                        new CustomToast(requireContext(), CustomToast.TOAST_ERROR, Toast.LENGTH_LONG).show("No se ha detectado ninguna aplicación para abrir documentos PDF.");
                    }
                } else if (file.getName().toLowerCase().endsWith(".jpg")) {
                    openFileIntent(Intent.ACTION_VIEW, SecretariaFileProvider.getUriForFile(requireContext(), file), "image/jpg");
                } else if (file.getName().toLowerCase().endsWith(".pdf")) {
                    Intent target = new Intent(Intent.ACTION_VIEW);
                    Uri uri = SecretariaFileProvider.getUriForFile(requireContext(), file);
                    target.setDataAndType(uri, "application/pdf");
                    target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Intent intent = Intent.createChooser(target, "Abrir documento PDF con...");

                    try {
                        requireContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Instruct the user to install a PDF reader here, or something
                        new CustomToast(requireContext(), CustomToast.TOAST_ERROR, Toast.LENGTH_LONG).show("No se ha detectado ninguna aplicación para abrir documentos PDF.");
                    }
                } else {
                    //No se reconoce cómo abrir este archivo
                    AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("No se puede abrir el archivo")

                            .create();

                    dialog.show();
                }
            }
        }
        renderFiles();
    }

    /**
     * Abre un intent para el archivo
     *
     * @param action   La acción del Intent
     * @param uri      El Uri del fichero a abrir
     * @param mimetype El MIME del fichero
     */
    @SuppressWarnings("SameParameterValue")
    private void openFileIntent(String action, Uri uri, String mimetype) {
        Intent intent = new Intent(action).setDataAndType(uri, mimetype);
        //Para que la actividad remota pueda abrir el fichero
        //SecretariaFileProvider.otorgarPermisos(requireContext(), intent);

		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        startActivity(intent);
    }

    /**
     * Este método muestra los ficheros en el directorio actual. También decide que directorios son visibles desde la raíz.
     */
    private void renderFiles() {
        FileAdapter adapter = (FileAdapter) listView.getAdapter();
        if (currentDirectory.equals(rootDirectory)) {
            //Si el usuario está en la raiz le mostramos unos nombres más familiares
            List<AliasedFile> files = new ArrayList<>();

            files.add(new AliasedFile("Logs", new File(rootDirectory.getAbsolutePath() + "/logs/"))
                    .setIcon(R.drawable.ic_log));


            adapter.changeListing(files.toArray(new AliasedFile[0]));

        } else {
            adapter.changeDirectory(new AliasedFile(currentDirectory));
        }
    }

    /**
     * Para renderizar los ficheros en la lista
     */
    private class FileAdapter extends ArrayAdapter<AliasedFile> {

        public int selectedFileCount = 0;

        public FileAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            AliasedFile alias = getItem(position);
            File file = alias.file;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_explorer, null);

            }
            CheckBox checkBox = convertView.findViewById(R.id.explorer_item_checkBox);
            TextView textView = convertView.findViewById(R.id.explorer_item_textBox);
            ImageView imageView = convertView.findViewById(R.id.explorer_item_imageView);
            checkBox.setVisibility(file.isFile() ? View.VISIBLE : View.GONE);
            if (alias.image != null)
                imageView.setImageResource(alias.image);
            imageView.setVisibility(alias.image != null ? View.VISIBLE : View.GONE);

            checkBox.setOnCheckedChangeListener((v, selected) -> {
                alias.isSelected = selected;
                selectedFileCount += (selected ? 1 : -1); //Si se ha seleccionado hacemos +1, si no -1

                //Activar o desactivar botones dependiendo de la cantidad de ficheros seleccionados
                buttonBorrar.setEnabled(true);
                buttonEnviar.setEnabled(true);

                if (selectedFileCount < 1) {
                    buttonEnviar.setEnabled(false);
                    buttonBorrar.setEnabled(false);
                }
            });

            checkBox.setChecked(getItem(position).isSelected);

            textView.setText(alias.toString());

            View finalConvertView = convertView; //Necesario para poder usar onItemClick, no me preguntes
            convertView.setOnClickListener((v) -> {
                listView.setSelection(position);
                //Ya que nuestros items son clickable, tenemos que reimplementar esto
                listView.getOnItemClickListener().onItemClick(listView, finalConvertView, position, 0);
            });

            return convertView;
        }

        public void changeListing(AliasedFile[] aliasedFiles) {
            selectedFileCount = 0;
            clear();
            addAll(aliasedFiles);
            notifyDataSetChanged();

            buttonBorrar.setVisibility(View.GONE);
        }

        public void changeDirectory(AliasedFile aliasedFile) {
            selectedFileCount = 0;
            clear();
            AliasedFile[] files = aliasedFile.listFiles();
            Arrays.sort(files);
            addAll(files);
            notifyDataSetChanged();

            //Ocultar el botón de borrado en directorios protegidos
            if (aliasedFile.file.getAbsolutePath().endsWith("/logs") ||
                    aliasedFile.file.getAbsolutePath().endsWith("/csv")) {
                buttonBorrar.setVisibility(View.VISIBLE);
            } else {
                buttonBorrar.setVisibility(View.GONE);
            }
        }

        public AliasedFile[] getSelectedFiles() {
            ArrayList<AliasedFile> files = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                AliasedFile file = getItem(i);
                if (file.isSelected) {
                    files.add(file);
                }
            }

            return files.toArray(new AliasedFile[0]);
        }

    }

    /**
     * Para poder hacer que el directorio root tenga nombres más reconocibles por el usuario<br />
     * Clase que contiene un <code>File</code> y opcionalmente un nombre distinto para mostrarlo al usuario
     */
    private static class AliasedFile implements Comparable<AliasedFile> {

        public String name;
        /**
         * El <code>File</code> representado por este <code>AliasedFile</code>
         */
        public File file;
        @DrawableRes
        public Integer image;

        public boolean isSelected;

        public AliasedFile(File file) {
            this.file = file;
        }

        public AliasedFile(String name, File file) {
            this.name = name;
            this.file = file;
        }

        public AliasedFile[] listFiles() {
            File[] files = file.listFiles();
            assert files != null;
            AliasedFile[] returnValue = new AliasedFile[files.length];
            for (int i = 0; i < files.length; i++) {
                returnValue[i] = new AliasedFile(files[i]);
            }

            return returnValue;
        }

        public AliasedFile setIcon(@DrawableRes Integer image) {
            this.image = image;
            return this;
        }

        @NonNull
        public String toString() {
            if (this.name != null && !this.name.isEmpty()) {
                //Devolver el alias del archivo
                return this.name;
            } else {
                //Devolver el nombre del archivo, con "/" añadido al final si es un directorio
                return file.getName() + (file.isDirectory() ? "/" : "");
            }
        }

        @Override
        public int compareTo(AliasedFile aliasedFile) {
            //Los directorios van primero
            if (this.file.isDirectory() && !aliasedFile.file.isDirectory()) {
                return -1;
            }
            //Si no, esto se ordena alfabéticamente
            return toString().compareTo(aliasedFile.toString());
        }
    }
}