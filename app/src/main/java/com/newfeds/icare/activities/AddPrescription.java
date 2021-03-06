package com.newfeds.icare.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.newfeds.icare.R;
import com.newfeds.icare.helper.DBhelper;
import com.newfeds.icare.helper.ImageHelper;
import com.newfeds.icare.helper.L;

import java.io.File;
import java.util.UUID;

public class AddPrescription extends AppCompatActivity {

    final int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    DBhelper dBhelper = null;
    Uri outputFileUri = null;
    String selectedImagePath = null;

    ImageView imageViewAddPrescription;
    Button buttonAddPrescriptionChooseFromGallary;
    EditText editTextAddPrescriptionTitle;
    EditText editTextAddPrescriptionDescription;
    Button buttonAddPrescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_prescription);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dBhelper = new DBhelper(this);

        imageViewAddPrescription=(ImageView)findViewById(R.id.imageViewAddPrescription);
        buttonAddPrescriptionChooseFromGallary=(Button)findViewById(R.id.buttonAddPrescriptionChooseFromGallary);
        editTextAddPrescriptionTitle=(EditText)findViewById(R.id.editTextAddPrescriptionTitle);
        editTextAddPrescriptionDescription=(EditText)findViewById(R.id.editTextAddPrescriptionDescription);
        buttonAddPrescription=(Button)findViewById(R.id.buttonAddPrescription);

        buttonAddPrescriptionChooseFromGallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        buttonAddPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPrescription();
                onBackPressed();
            }
        });

    }

    private void addPrescription(){
        boolean allOk = true;

        if(allOk){
            dBhelper.inputPrescription(MemberDashboard.memberId,editTextAddPrescriptionTitle.getText().toString(),
                    editTextAddPrescriptionDescription.getText().toString(), ImageHelper.saveImage(this, outputFileUri));
            onBackPressed();
        }
    }



    private void selectImage(){
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(intent.resolveActivity(getPackageManager())!=null){
                        File photofile = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                        outputFileUri = Uri.fromFile(photofile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                } else if (items[item].equals("Choose from Library")) {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                    startActivityForResult(chooserIntent, SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        String uri = outputFileUri.toString();
        L.log("Uri: " + uri);

        if(outputFileUri!=null){
            L.log(new File(outputFileUri.getPath()).getAbsolutePath());
            selectedImagePath = new File(outputFileUri.getPath()).getAbsolutePath();
            L.log("Selected: " + selectedImagePath);


            Glide.with(this).load(new File(selectedImagePath))
                    .skipMemoryCache(true)
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .into(imageViewAddPrescription);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        outputFileUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(outputFileUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);
        L.log("SelectedImagePath: " + selectedImagePath);


        Glide.with(this).load(new File(selectedImagePath))
                .skipMemoryCache(true)
                .signature(new StringSignature(UUID.randomUUID().toString()))
                .into(imageViewAddPrescription);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
