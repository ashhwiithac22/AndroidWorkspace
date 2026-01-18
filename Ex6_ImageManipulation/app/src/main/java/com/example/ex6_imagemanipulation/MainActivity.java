package com.example.ex6_imagemanipulation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_PICK_1 = 2;
    private static final int REQUEST_IMAGE_PICK_2 = 3;
    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_CROP_IMAGE = 101;

    private ImageView imageView;
    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    private Bitmap mergeBitmap1, mergeBitmap2;
    private GridView featuresGrid;
    private boolean isCropping = false;
    private int cropX, cropY, cropWidth, cropHeight;

    // Feature names and emoji icons
    private final String[] featureNames = {
            "Crop Image", "Apply Filter", "Resize Image",
            "Merge Images", "Add Mask", "Rotate Image"
    };

    private final String[] featureIcons = {"✂️", "🎨", "📐", "🖼️", "⭕", "↻"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupFeatureGrid();
        checkPermissions();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        featuresGrid = findViewById(R.id.featuresGrid);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnSaveImage = findViewById(R.id.btnSaveImage);

        // Set click listeners
        imageView.setOnClickListener(v -> selectImage());
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSaveImage.setOnClickListener(v -> saveImage());

        // Create placeholder
        createPlaceholderImage();

        // Set up touch listener for cropping
        imageView.setOnTouchListener((v, event) -> {
            if (isCropping) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        cropX = (int) x;
                        cropY = (int) y;
                        break;
                    case android.view.MotionEvent.ACTION_MOVE:
                        cropWidth = (int) (x - cropX);
                        cropHeight = (int) (y - cropY);
                        // Draw crop rectangle on image
                        drawCropRectangle();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                        if (cropWidth > 10 && cropHeight > 10) {
                            performCrop();
                        }
                        isCropping = false;
                        break;
                }
                return true;
            }
            return false;
        });
    }

    private void drawCropRectangle() {
        if (currentBitmap != null) {
            Bitmap tempBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(tempBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawRect(cropX, cropY, cropX + cropWidth, cropY + cropHeight, paint);
            imageView.setImageBitmap(tempBitmap);
        }
    }

    private void setupFeatureGrid() {
        List<Map<String, Object>> featureList = new ArrayList<>();

        for (int i = 0; i < featureNames.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("icon", featureIcons[i]);
            map.put("name", featureNames[i]);
            featureList.add(map);
        }

        String[] from = {"icon", "name"};
        int[] to = {R.id.featureIcon, R.id.featureName};

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                featureList,
                R.layout.grid_item_feature,
                from,
                to
        );

        featuresGrid.setAdapter(adapter);
        featuresGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentBitmap == null && position != 3) { // Merge can work without initial image
                    Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (position) {
                    case 0: // Crop
                        startCropMode();
                        break;
                    case 1: // Filter
                        showFilterDialog();
                        break;
                    case 2: // Resize
                        showResizeDialog();
                        break;
                    case 3: // Merge
                        showMergeDialog();
                        break;
                    case 4: // Mask
                        applyCircularMask();
                        break;
                    case 5: // Rotate
                        rotateImage();
                        break;
                }
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_PERMISSION);
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                switch (requestCode) {
                    case REQUEST_IMAGE_PICK:
                        originalBitmap = bitmap;
                        currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        imageView.setImageBitmap(currentBitmap);
                        findViewById(R.id.noImageText).setVisibility(View.GONE);
                        Toast.makeText(this, "Image loaded successfully!", Toast.LENGTH_SHORT).show();
                        break;

                    case REQUEST_IMAGE_PICK_1:
                        mergeBitmap1 = bitmap;
                        Toast.makeText(this, "First image selected for merge", Toast.LENGTH_SHORT).show();
                        break;

                    case REQUEST_IMAGE_PICK_2:
                        mergeBitmap2 = bitmap;
                        Toast.makeText(this, "Second image selected for merge", Toast.LENGTH_SHORT).show();
                        // Auto-merge if both images are selected
                        if (mergeBitmap1 != null && mergeBitmap2 != null) {
                            mergeSelectedImages();
                        }
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Feature 1: Interactive Cropping
    private void startCropMode() {
        isCropping = true;
        Toast.makeText(this, "Drag on image to select crop area", Toast.LENGTH_LONG).show();
    }

    private void performCrop() {
        if (cropWidth > 0 && cropHeight > 0) {
            // Ensure crop coordinates are within bounds
            int x = Math.max(0, Math.min(cropX, currentBitmap.getWidth() - 1));
            int y = Math.max(0, Math.min(cropY, currentBitmap.getHeight() - 1));
            int width = Math.min(cropWidth, currentBitmap.getWidth() - x);
            int height = Math.min(cropHeight, currentBitmap.getHeight() - y);

            if (width > 0 && height > 0) {
                currentBitmap = Bitmap.createBitmap(currentBitmap, x, y, width, height);
                imageView.setImageBitmap(currentBitmap);
                Toast.makeText(this, "✓ Image cropped!", Toast.LENGTH_SHORT).show();
            }
        }
        isCropping = false;
    }

    // Feature 2: Filtering
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎨 Apply Filter");

        String[] filters = {
                "Grayscale", "Sepia", "Invert Colors",
                "Brightness +", "Contrast +", "Vintage"
        };

        builder.setItems(filters, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap filteredBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);

                switch (which) {
                    case 0: // Grayscale
                        filteredBitmap = applyGrayscale(filteredBitmap);
                        break;
                    case 1: // Sepia
                        filteredBitmap = applySepia(filteredBitmap);
                        break;
                    case 2: // Invert
                        filteredBitmap = invertColors(filteredBitmap);
                        break;
                    case 3: // Brightness
                        filteredBitmap = adjustBrightness(filteredBitmap, 50);
                        break;
                    case 4: // Contrast
                        filteredBitmap = adjustContrast(filteredBitmap, 1.5f);
                        break;
                    case 5: // Vintage
                        filteredBitmap = applyVintage(filteredBitmap);
                        break;
                }

                currentBitmap = filteredBitmap;
                imageView.setImageBitmap(currentBitmap);
                Toast.makeText(MainActivity.this, "✓ " + filters[which] + " applied!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private Bitmap applyGrayscale(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return result;
    }

    private Bitmap applySepia(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        ColorMatrix matrix = new ColorMatrix(new float[] {
                0.393f, 0.769f, 0.189f, 0, 0,
                0.349f, 0.686f, 0.168f, 0, 0,
                0.272f, 0.534f, 0.131f, 0, 0,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return result;
    }

    private Bitmap invertColors(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = bitmap.getPixel(x, y);
                int alpha = Color.alpha(pixel);
                int red = 255 - Color.red(pixel);
                int green = 255 - Color.green(pixel);
                int blue = 255 - Color.blue(pixel);
                result.setPixel(x, y, Color.argb(alpha, red, green, blue));
            }
        }

        return result;
    }

    private Bitmap adjustBrightness(Bitmap bitmap, int value) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        ColorMatrix matrix = new ColorMatrix(new float[] {
                1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return result;
    }

    private Bitmap adjustContrast(Bitmap bitmap, float contrast) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        float scale = contrast;
        float translate = (1 - scale) * 0.5f * 255;

        ColorMatrix matrix = new ColorMatrix(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return result;
    }

    private Bitmap applyVintage(Bitmap bitmap) {
        Bitmap result = applySepia(bitmap);
        result = adjustBrightness(result, -20);
        result = adjustContrast(result, 0.8f);
        return result;
    }

    // Feature 3: Resizing (FIXED)
    private void showResizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📐 Resize Image");
        builder.setMessage("Select new size:");

        String[] sizes = {
                "Small (50%)",
                "Medium (75%)",
                "Large (125%)",
                "Custom Size",
                "Instagram Square (1080x1080)",
                "HD Wallpaper (1920x1080)"
        };

        builder.setItems(sizes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int newWidth, newHeight;
                int originalWidth = currentBitmap.getWidth();
                int originalHeight = currentBitmap.getHeight();

                switch (which) {
                    case 0: // Small (50%)
                        newWidth = originalWidth / 2;
                        newHeight = originalHeight / 2;
                        break;
                    case 1: // Medium (75%)
                        newWidth = (int) (originalWidth * 0.75);
                        newHeight = (int) (originalHeight * 0.75);
                        break;
                    case 2: // Large (125%)
                        newWidth = (int) (originalWidth * 1.25);
                        newHeight = (int) (originalHeight * 1.25);
                        break;
                    case 3: // Custom Size
                        showCustomSizeDialog();
                        return;
                    case 4: // Instagram Square
                        newWidth = 1080;
                        newHeight = 1080;
                        break;
                    case 5: // HD Wallpaper
                        newWidth = 1920;
                        newHeight = 1080;
                        break;
                    default:
                        return;
                }

                // Ensure minimum size
                newWidth = Math.max(1, newWidth);
                newHeight = Math.max(1, newHeight);

                // Resize the bitmap
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true);
                currentBitmap = resizedBitmap;
                imageView.setImageBitmap(currentBitmap);
                Toast.makeText(MainActivity.this,
                        "✓ Resized to " + newWidth + "x" + newHeight,
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCustomSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Custom Size");

        // Get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        builder.setMessage("Current image: " + currentBitmap.getWidth() + "x" + currentBitmap.getHeight() +
                "\nScreen size: " + screenWidth + "x" + screenHeight +
                "\n\nSuggested sizes:\n1. 800x600\n2. 1024x768\n3. 1280x720");

        builder.setPositiveButton("800x600", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resizeToCustom(800, 600);
            }
        });

        builder.setNeutralButton("1024x768", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resizeToCustom(1024, 768);
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void resizeToCustom(int width, int height) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(currentBitmap, width, height, true);
        currentBitmap = resizedBitmap;
        imageView.setImageBitmap(currentBitmap);
        Toast.makeText(this, "✓ Resized to " + width + "x" + height, Toast.LENGTH_SHORT).show();
    }

    // Feature 4: Merging Two Images (FIXED)
    private void showMergeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Merge Two Images");
        builder.setMessage("Select two images to merge:");

        builder.setPositiveButton("Select First Image", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK_1);
            }
        });

        builder.setNegativeButton("Select Second Image", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK_2);
            }
        });

        builder.setNeutralButton("Merge Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mergeBitmap1 != null && mergeBitmap2 != null) {
                    mergeSelectedImages();
                } else {
                    Toast.makeText(MainActivity.this, "Please select both images first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.show();
    }

    private void mergeSelectedImages() {
        if (mergeBitmap1 == null || mergeBitmap2 == null) {
            Toast.makeText(this, "Please select both images first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Resize both images to same dimensions (use the smaller dimensions)
        int width = Math.min(mergeBitmap1.getWidth(), mergeBitmap2.getWidth());
        int height = Math.min(mergeBitmap1.getHeight(), mergeBitmap2.getHeight());

        Bitmap resizedBitmap1 = Bitmap.createScaledBitmap(mergeBitmap1, width, height, true);
        Bitmap resizedBitmap2 = Bitmap.createScaledBitmap(mergeBitmap2, width, height, true);

        // Create a new bitmap for the merged result
        Bitmap mergedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergedBitmap);

        // Draw first image
        canvas.drawBitmap(resizedBitmap1, 0, 0, null);

        // Draw second image with transparency (50% opacity)
        Paint paint = new Paint();
        paint.setAlpha(128); // 50% opacity
        canvas.drawBitmap(resizedBitmap2, 0, 0, paint);

        // Add border
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLUE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10);
        canvas.drawRect(0, 0, width, height, borderPaint);

        // Add merge text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Merged Image", width / 2, height - 50, textPaint);

        // Set as current image
        currentBitmap = mergedBitmap;
        originalBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(currentBitmap);
        findViewById(R.id.noImageText).setVisibility(View.GONE);

        Toast.makeText(this, "✓ Images merged successfully!", Toast.LENGTH_SHORT).show();

        // Reset merge bitmaps
        mergeBitmap1 = null;
        mergeBitmap2 = null;
    }

    // Feature 5: Masking
    private void applyCircularMask() {
        if (currentBitmap == null) return;

        int width = currentBitmap.getWidth();
        int height = currentBitmap.getHeight();
        int size = Math.min(width, height);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Create circular mask
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);

        // Draw circular background
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);

        // Set up paint for mask
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Draw image with circular mask
        canvas.drawBitmap(currentBitmap,
                (size - width) / 2,
                (size - height) / 2,
                paint);

        // Add border to circular image
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10);
        borderPaint.setAntiAlias(true);
        canvas.drawCircle(size / 2, size / 2, size / 2, borderPaint);

        currentBitmap = output;
        imageView.setImageBitmap(currentBitmap);
        Toast.makeText(this, "✓ Circular mask applied!", Toast.LENGTH_SHORT).show();
    }

    // Feature 6: Rotate
    private void rotateImage() {
        if (currentBitmap == null) return;

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        currentBitmap = Bitmap.createBitmap(currentBitmap, 0, 0,
                currentBitmap.getWidth(), currentBitmap.getHeight(), matrix, true);

        imageView.setImageBitmap(currentBitmap);
        Toast.makeText(this, "✓ Rotated 90°", Toast.LENGTH_SHORT).show();
    }

    private void saveImage() {
        if (currentBitmap == null) {
            Toast.makeText(this, "⚠️ No image to save", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create directory if not exists
            File directory = new File(getExternalFilesDir(null), "Ex6_Images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "processed_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            fos.close();

            // Update gallery
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), fileName, "Image processed by Ex6 App");

            Toast.makeText(this, "✅ Image saved to:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPlaceholderImage() {
        // Create gradient placeholder
        Bitmap placeholder = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(placeholder);
        Paint paint = new Paint();

        // Gradient background
        paint.setShader(new android.graphics.LinearGradient(
                0, 0, 600, 400,
                Color.parseColor("#667eea"),
                Color.parseColor("#764ba2"),
                android.graphics.Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, 600, 400, paint);

        // Draw camera icon
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("📸", 300, 200, paint);

        // Draw text
        paint.setTextSize(40);
        canvas.drawText("Tap to select image", 300, 320, paint);

        imageView.setImageBitmap(placeholder);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Some features may not work without permissions",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}