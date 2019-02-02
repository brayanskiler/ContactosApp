package com.example.jcgo1.ejemplo1;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import static android.provider.ContactsContract.*;

public class MainActivity extends AppCompatActivity {

    private final int PICK_CONTACT_REQUEST = 1;
    private Uri contactoUri;
    private  final int PERMISSIONS_REQUEST_READ_CONTACT = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Asignar permisos en tiempo de ejecucion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACT);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_REQUEST_READ_CONTACT:
                //Verifica que los contactos hay sido otorgados
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    return;
                }else{
                    //se cierra la aplicacion
                    finish();
                }
        }
    }

    public void initSeleccionarContacto(View v){
        Intent i = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(i,PICK_CONTACT_REQUEST);
    }

    private void recibirContacto(Uri uri) {
        TextView nombre = (TextView) findViewById(R.id.nombreContacto);
        TextView telefono = (TextView) findViewById(R.id.telefonoContacto);
        ImageView foto = (ImageView) findViewById(R.id.fotoContacto);

        nombre.setText(getNombre(uri));
        telefono.setText(getTelefono(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                contactoUri = data.getData();
                recibirContacto(contactoUri);
            }
        }
    }

    //OBTENER EL NOMBRE DEL CONTACTO
    private String getNombre(Uri uri) {
        String nombre = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(
                uri,
                new String[]{Contacts.DISPLAY_NAME},
                null,
                null,
                null
        );
        if(c.moveToFirst()){
            nombre = c.getString(0);
        }
        c.close();
        return nombre;
    }

    private String refreshData() {
        String emaildata = "";

        try {

            ContentResolver cr = getBaseContext()
                    .getContentResolver();
            Cursor cur = cr
                    .query(ContactsContract.Contacts.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

            if (cur.getCount() > 0) {

                Log.i("Content provider", "Reading contact  emails");

                while (cur
                        .moveToNext()) {

                    String contactId = cur
                            .getString(cur
                                    .getColumnIndex(ContactsContract.Contacts._ID));

                    // Create query to use CommonDataKinds classes to fetch emails
                    Cursor emails = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                    + " = " + contactId, null, null);
                    while (emails.moveToNext()) {

                        // This would allow you get several email addresses
                        String emailAddress = emails
                                .getString(emails
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                        //Log.e("email==>", emailAddress);

                        emaildata +=" "+emailAddress+" "
                                +"--------------------------------------";
                    }
                    emails.close();
                }

            }
            else
            {
                emaildata += " Data not found. ";

            }
            cur.close();


        } catch (Exception e) {

            emaildata +=" Exception : "+e+" ";
        }

        return emaildata;
    }



    // TOMAR EL NUMERO (TELEFONO)DEL CONTACTO
    private String getTelefono(Uri uri){
        String id = null;
        String telefono = null;

        //toma el id del contacto del  que se quiere el numero telefonico
        Cursor contactoCursor = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        );


        if (contactoCursor.moveToFirst()){
            id= contactoCursor.getString(0);
        }

        //Hace la compracion de el contacato id de la tabla teleonos es igual al id  y el
        // telefono es ifual al telefono mobil
        contactoCursor.close();
        String selectionArgs = CommonDataKinds.Phone.CONTACT_ID + "= ? AND " +
                CommonDataKinds.Phone.TYPE + "=" +
                CommonDataKinds.Phone.TYPE_MOBILE;

        Cursor telefonoCursor = getContentResolver().query(
         CommonDataKinds.Phone.CONTENT_URI,
                new String[]{CommonDataKinds.Phone.NUMBER},
                selectionArgs,
                new String[]{id},
                null
        );

if (telefonoCursor.moveToFirst()){
    telefono = telefonoCursor.getString(0);
}
telefonoCursor.close();

        return  telefono;
    }


    private Bitmap getFoto(Uri uri){
        Bitmap foto = null;
        String id  = null;

        Cursor contactoCursor = getContentResolver().query(
                uri,
                new String[]{Contacts._ID},
                null,
                null,null
        );
        if (contactoCursor.moveToFirst()){
            id = contactoCursor.getString(0);
        }

        contactoCursor.close();

        try {
            Uri contactoUri  = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.parseLong(id));
            InputStream input = Contacts.openContactPhotoInputStream(
              getContentResolver(),
              contactoUri
            );

            if (input != null){
                foto = BitmapFactory.decodeStream(input);
                input.close();
            }

        }catch (IOException ioe){
            //Manejar algun error de entrada y salida
        }

        return foto;
    }


}
