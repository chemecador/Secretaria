package com.chemecador.secretaria.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider.getUriForFile
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.gui.CustomToast
import com.chemecador.secretaria.interfaces.OnBackPressed
import com.chemecador.secretaria.logger.Logger
import com.chemecador.secretaria.provider.SecretariaFileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.Arrays
import java.util.Locale

class ExplorerFragment : Fragment(), OnBackPressed {
    private var currentDirectory: File? = null
    private var rootDirectory: File? = null
    private val history: MutableList<AliasedFile?>
    private var listView: ListView? = null
    private var adapter: FileAdapter? = null
    private var buttonEnviar: Button? = null
    private var buttonBorrar: Button? = null

    init {
        // Required empty public constructor
        history = ArrayList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (currentDirectory == null) currentDirectory = context.filesDir
        rootDirectory = context.filesDir
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.explorer_listView)
        adapter = FileAdapter(requireContext())
        listView?.adapter = adapter
        listView?.onItemClickListener =
            OnItemClickListener { adapterView: AdapterView<*>, view: View, position: Int, id: Long ->
                fileSelected(
                    adapterView,
                    view,
                    position,
                    id
                )
            }
        buttonBorrar = view.findViewById(R.id.explorer_delete)
        buttonEnviar = view.findViewById(R.id.explorer_send)
        buttonBorrar?.setOnClickListener {
            var couldDeleteAll = true
            for (file in adapter?.selectedFiles!!) {
                //Borrar solo si es un log
                if (file!!.file!!.absolutePath.contains("/logs/") && file.file!!.name.endsWith(".txt") ||
                    file.file!!.absolutePath.contains("/csv/")
                ) {
                    if (Logger.singleton?.file == file.file) {
                        couldDeleteAll = false
                    } else {
                        if (!file.file!!.delete()) couldDeleteAll = false
                    }
                } else {
                    Logger.w(
                        ExplorerFragment.className,
                        "Se intentó borrar el fichero " + file.file + " pero no se reconoció como fichero que el usuario deberia poder borrar"
                    )
                    couldDeleteAll = false
                }
            }
            if (!couldDeleteAll) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage("No se pudieron borrar todos los ficheros")
                    .create().show()
            }
            renderFiles()
        }
        buttonEnviar?.setOnClickListener {
            val files = ArrayList<Uri>()
            for (file in adapter?.selectedFiles!!) {
                files.add(
                    getUriForFile(
                        requireContext(),
                        SecretariaFileProvider.authority,
                        file!!.file!!
                    )
                )
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
                .setType("*/*")
                .putExtra(Intent.EXTRA_STREAM, files)
            if (currentDirectory!!.absolutePath.endsWith("/logs")) {
                //TODO cambiar dirección de correo
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("chemecador@gmail.com"))
            }
            startActivity(intent)
        }
        renderFiles()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explorer, container, false)
    }

    override fun onBackPressed(): Boolean {
        return if (history.size == 0) {
            false
        } else {
            history.removeAt(history.size - 1)
            currentDirectory = if (history.size > 0) {
                history[history.size - 1]!!.file
            } else {
                rootDirectory
            }
            renderFiles()
            true
        }
    }

    /**
     * Se ejecuta al seleccionar un fichero<br></br>
     *
     *
     * Si quieres añadir un fichero para que sea visible ten en cuenta que:<br></br>
     * · Tienes que añadirlo en `exported_files.xml`<br></br>
     * · Tienes que hacer su directorio accesible añadiendo un item en `renderFiles()`
     */
    private fun fileSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
        //Al entrar en un directorio, cambiar a ese
        val adapter = listView!!.adapter as FileAdapter
        val alias = adapter.getItem(position)!!
        val file = alias.file
        if (file?.isDirectory == true) {
            if (file.listFiles()?.isEmpty() == true) {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Aviso")
                    .setMessage("No se han encontrado archivos")
                    .create()
                dialog.show()
            } else {
                history.add(alias)
                currentDirectory = file
            }
        } else {
            if (file?.exists() == false) {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Aviso")
                    .setTitle("No se han encontrado archivos")
                    .create()
                dialog.show()
            } else {
                if (file?.name?.lowercase(Locale.getDefault())?.endsWith(".txt") == true) {
                    openFileIntent(
                        Intent.ACTION_VIEW,
                        SecretariaFileProvider.getUriForFile(requireContext(), file),
                        "text/plain"
                    )
                } else if (file?.name?.lowercase(Locale.getDefault())?.endsWith(".csv") == true) {
                    //openFileIntent(Intent.ACTION_VIEW, SecretariaFileProvider.getUriForFile(requireContext(), file), "text/csv");
                    val target = Intent(Intent.ACTION_VIEW)
                    val uri = SecretariaFileProvider.getUriForFile(requireContext(), file)
                    target.setDataAndType(uri, "text/csv")
                    target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val intent = Intent.createChooser(target, "Abrir documento csv con...")
                    try {
                        requireContext().startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Instruct the user to install a PDF reader here, or something
                        CustomToast(
                            requireContext(),
                            CustomToast.TOAST_ERROR,
                            Toast.LENGTH_LONG
                        ).show("No se ha detectado ninguna aplicación para abrir documentos PDF.")
                    }
                } else if (file?.name?.lowercase(Locale.getDefault())?.endsWith(".jpg") == true) {
                    openFileIntent(
                        Intent.ACTION_VIEW,
                        SecretariaFileProvider.getUriForFile(requireContext(), file),
                        "image/jpg"
                    )
                } else if (file?.name?.lowercase(Locale.getDefault())?.endsWith(".pdf") == true) {
                    val target = Intent(Intent.ACTION_VIEW)
                    val uri = SecretariaFileProvider.getUriForFile(requireContext(), file)
                    target.setDataAndType(uri, "application/pdf")
                    target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val intent = Intent.createChooser(target, "Abrir documento PDF con...")
                    try {
                        requireContext().startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Instruct the user to install a PDF reader here, or something
                        CustomToast(
                            requireContext(),
                            CustomToast.TOAST_ERROR,
                            Toast.LENGTH_LONG
                        ).show("No se ha detectado ninguna aplicación para abrir documentos PDF.")
                    }
                } else {
                    //No se reconoce cómo abrir este archivo
                    val dialog = MaterialAlertDialogBuilder(requireContext())
                        .setTitle("No se puede abrir el archivo")
                        .create()
                    dialog.show()
                }
            }
        }
        renderFiles()
    }

    /**
     * Abre un intent para el archivo
     *
     * @param action   La acción del Intent
     * @param uri      El Uri del fichero a abrir
     * @param mimetype El MIME del fichero
     */
    private fun openFileIntent(action: String, uri: Uri, mimetype: String) {
        val intent = Intent(action).setDataAndType(uri, mimetype)
        //Para que la actividad remota pueda abrir el fichero
        //SecretariaFileProvider.otorgarPermisos(requireContext(), intent);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivity(intent)
    }

    /**
     * Este método muestra los ficheros en el directorio actual. También decide que directorios son visibles desde la raíz.
     */
    private fun renderFiles() {
        val adapter = listView!!.adapter as FileAdapter
        if (currentDirectory == rootDirectory) {
            //Si el usuario está en la raiz le mostramos unos nombres más familiares
            val files: MutableList<AliasedFile> = ArrayList()
            files.add(
                AliasedFile("Logs", File(rootDirectory!!.absolutePath + "/logs/"))
                    .setIcon(R.drawable.ic_log)
            )
            adapter.changeListing(files.toTypedArray())
        } else {
            adapter.changeDirectory(AliasedFile(currentDirectory))
        }
    }

    /**
     * Para renderizar los ficheros en la lista
     */
    private inner class FileAdapter(context: Context?) : ArrayAdapter<AliasedFile?>(
        context!!, android.R.layout.simple_list_item_1
    ) {
        var selectedFileCount = 0
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val alias = getItem(position)
            val file = alias!!.file
            if (convertView == null) {
                convertView =
                    LayoutInflater.from(context).inflate(R.layout.list_item_explorer, parent, false)
            }
            val checkBox = convertView!!.findViewById<CheckBox>(R.id.explorer_item_checkBox)
            val textView = convertView.findViewById<TextView>(R.id.explorer_item_textBox)
            val imageView = convertView.findViewById<ImageView>(R.id.explorer_item_imageView)
            checkBox.visibility = if (file!!.isFile) View.VISIBLE else View.GONE
            if (alias.image != null) imageView.setImageResource(alias.image!!)
            imageView.visibility = if (alias.image != null) View.VISIBLE else View.GONE
            checkBox.setOnCheckedChangeListener { _: CompoundButton?, selected: Boolean ->
                alias.isSelected = selected
                selectedFileCount += if (selected) 1 else -1 //Si se ha seleccionado hacemos +1, si no -1

                //Activar o desactivar botones dependiendo de la cantidad de ficheros seleccionados
                buttonBorrar!!.isEnabled = true
                buttonEnviar!!.isEnabled = true
                if (selectedFileCount < 1) {
                    buttonEnviar!!.isEnabled = false
                    buttonBorrar!!.isEnabled = false
                }
            }
            checkBox.isChecked = getItem(position)!!.isSelected
            textView.text = alias.toString()
            val finalConvertView =
                convertView //Necesario para poder usar onItemClick, no me preguntes
            convertView.setOnClickListener {
                listView!!.setSelection(position)
                //Ya que nuestros items son clickable, tenemos que reimplementar esto
                listView!!.onItemClickListener!!
                    .onItemClick(listView, finalConvertView, position, 0)
            }
            return convertView
        }

        fun changeListing(aliasedFiles: Array<AliasedFile>) {
            selectedFileCount = 0
            clear()
            addAll(*aliasedFiles)
            notifyDataSetChanged()
            buttonBorrar!!.visibility = View.GONE
        }

        fun changeDirectory(aliasedFile: AliasedFile) {
            selectedFileCount = 0
            clear()
            val files = aliasedFile.listFiles()
            Arrays.sort(files)
            addAll(*files)
            notifyDataSetChanged()

            //Ocultar el botón de borrado en directorios protegidos
            if (aliasedFile.file!!.absolutePath.endsWith("/logs") ||
                aliasedFile.file!!.absolutePath.endsWith("/csv")
            ) {
                buttonBorrar!!.visibility = View.VISIBLE
            } else {
                buttonBorrar!!.visibility = View.GONE
            }
        }

        val selectedFiles: Array<AliasedFile?>
            get() {
                val files = ArrayList<AliasedFile?>()
                for (i in 0 until count) {
                    val file = getItem(i)
                    if (file!!.isSelected) {
                        files.add(file)
                    }
                }
                return files.toTypedArray()
            }
    }

    /**
     * Para poder hacer que el directorio root tenga nombres más reconocibles por el usuario<br></br>
     * Clase que contiene un `File` y opcionalmente un nombre distinto para mostrarlo al usuario
     */
    private class AliasedFile : Comparable<AliasedFile> {
        var name: String? = null

        /**
         * El `File` representado por este `AliasedFile`
         */
        var file: File?

        @DrawableRes
        var image: Int? = null
        var isSelected = false

        constructor(file: File?) {
            this.file = file
        }

        constructor(name: String?, file: File?) {
            this.name = name
            this.file = file
        }

        fun listFiles(): Array<AliasedFile?> {
            val files = file!!.listFiles()!!
            val returnValue = arrayOfNulls<AliasedFile>(files.size)
            for (i in files.indices) {
                returnValue[i] = AliasedFile(files[i])
            }
            return returnValue
        }

        fun setIcon(@DrawableRes image: Int?): AliasedFile {
            this.image = image
            return this
        }

        override fun toString(): String {
            return name!!.ifEmpty {
                //Devolver el nombre del archivo, con "/" añadido al final si es un directorio
                file!!.name + if (file!!.isDirectory) "/" else ""
            }
        }

        override fun compareTo(other: AliasedFile): Int {
            //Los directorios van primero
            return if (file!!.isDirectory && !other.file!!.isDirectory) {
                -1
            } else toString().compareTo(
                other.toString()
            )
            //Si no, esto se ordena alfabéticamente
        }
    }

    companion object {
        private val className = ExplorerFragment::class.java.simpleName
    }
}