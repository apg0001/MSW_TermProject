package com.example.mswproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Calendar;

public class MealInputActivity extends AppCompatActivity {

    private EditText editTextDish, editTextCost, editTextReview;
    private DBHelper dbHelper;

    private static final int PICK_PHOTO_REQUEST = 1;
    private ImageView imageViewPhoto;
    private String selectedPhotoPath;

    private Spinner spinnerOption, spinnerCategory, spinnerPlace;
    private EditText editTextStartTime, editTextEndTime;
    private String selectedStartTime, selectedEndTime, selectedStartDate, selectedEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meal_input_layout);

        // Initialize EditText fields
        editTextDish = findViewById(R.id.editTextDish);
        editTextCost = findViewById(R.id.editTextCost);
        editTextReview = findViewById(R.id.editTextReview);
        editTextStartTime = findViewById(R.id.editTextStartTime);
        editTextEndTime = findViewById(R.id.editTextEndTime);

        // Set up adapters for spinners
        spinnerOption = findViewById(R.id.spinnerOption);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPlace = findViewById(R.id.spinnerPlace);
        setupSpinnerAdapter(spinnerOption, R.array.options_array);
        setupSpinnerAdapter(spinnerCategory, R.array.categories_array);
        setupSpinnerAdapter(spinnerPlace, R.array.places_array);

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        // Initialize ImageView
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

        // Set listeners for date and time pickers
        setDateTimePickerListeners();
    }

    private void setupSpinnerAdapter(Spinner spinner, int arrayResourceId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResourceId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setDateTimePickerListeners() {
        editTextStartTime.setOnClickListener(v -> showDateTimePicker(true));
        editTextEndTime.setOnClickListener(v -> showDateTimePicker(false));
    }

    private void showDateTimePicker(final boolean isStartTime) {
        // Get the current date
        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Format the selected date
                    String formattedDate = String.format("%04d%02d%02d", year1, monthOfYear + 1, dayOfMonth);
                    if (isStartTime) {
                        selectedStartDate = formattedDate;
                    } else {
                        selectedEndDate = formattedDate;
                    }

                    // Show the TimePickerDialog after setting the date
                    showTimePicker(isStartTime);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker(final boolean isStartTime) {
        // Get the current time
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // Format the selected time and update the EditText
                    String formattedTime = String.format("%02d%02d", hourOfDay, minute);
                    if (isStartTime) {
                        selectedStartTime = selectedStartDate + formattedTime;
                        String dateTemp = selectedStartTime.substring(0, 4) + "/" + selectedStartTime.substring(4, 6) + "/" + selectedStartTime.substring(6, 8) + " " +
                                selectedStartTime.substring(8, 10) + ":" + selectedStartTime.substring(10, 12);
                        editTextStartTime.setText(dateTemp);
                    } else {
                        selectedEndTime = selectedEndDate + formattedTime;
                        String dateTemp = selectedEndTime.substring(0, 4) + "/" + selectedEndTime.substring(4, 6) + "/" + selectedEndTime.substring(6, 8) + " " +
                                selectedEndTime.substring(8, 10) + ":" + selectedEndTime.substring(10, 12);
                        editTextEndTime.setText(dateTemp);
                    }
                },
                currentHour,
                currentMinute,
                true // 24-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

    public void pickPhoto(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            // Handle the photo picked from the gallery
            Uri selectedImage = data.getData();
            // Get the actual path from the URI (you may need to handle different schemes)
            selectedPhotoPath = getRealPathFromURI(selectedImage);
            // Display the selected photo
            loadImage(selectedPhotoPath);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor == null) {
            return contentUri.getPath(); // If null, just return the original path
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(projection[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    private void loadImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageViewPhoto.setImageBitmap(myBitmap);
        }
    }

    private static int calculateUnicodeSum(String input){
        int sum = 0;

        for(int i = 0; i < input.length(); i++){
            char character = input.charAt(i);
            int unicodeValue = character;

            sum += unicodeValue;
        }
        return sum;
    }

    public void saveMeal(View view) {
        // Retrieve user input
        String dish = editTextDish.getText().toString();
        String option = spinnerOption.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String place = spinnerPlace.getSelectedItem().toString();
        String cost = editTextCost.getText().toString();
        String review = editTextReview.getText().toString();

        // Save input to the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dish", dish);
        values.put("option", option);
        values.put("category", category);
        values.put("start_time", selectedStartTime);
        values.put("end_time", selectedEndTime);
        values.put("place", place);
        values.put("cost", cost.isEmpty() ? 0 : Integer.parseInt(cost));
        values.put("review", review);
        values.put("photo_path", selectedPhotoPath);

        Random random = new Random(calculateUnicodeSum(dish));
        values.put("calories", random.nextInt(500) + 500);

        long newRowId = db.insert("meallist", null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Meal saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error saving meal", Toast.LENGTH_SHORT).show();
        }

        // Close the database
        dbHelper.close();

        Intent intent = new Intent(getApplicationContext(), MealList.class);
        startActivity((intent));
        finish();
    }
}