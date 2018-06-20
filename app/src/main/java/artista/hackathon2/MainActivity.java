package artista.hackathon2;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.gson.Gson;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{

    private static final int PHOTO_REQUEST = 10;
    private TextView scanResults;
    private Uri imageUri;
    private TextRecognizer detector;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static RequestQueue queue;
    private static RecyclerView recyclerView;
    private static ArrayList<ProductModal> prodList;
    private static double totalPrice;
    private static final String EMPTY_IMAGE = "https://previews.123rf.com/images/yupiramos/yupiramos1708/yupiramos170812318/83821425-page-not-found-404-error-vector-icon-illustration-design-graphic.jpg";
    static private ProgressDialog pd;
    private static ProgressBar pb;
    public static ArrayList<BuyhatkeResults[]> buyhatkeResults;
    private static Activity context;
    private static int posInFocus;
    private static HashMap<String, Double> FOOD_LIST;
    private static final String SWIGGY_URL = "https://www.swiggy.com/";
    private static final String FOOD_IMAGE = "http://www.blueroofchurch.com/wp-content/uploads/2015/01/Food-Pantry-Thumbnail-450x450.jpg";
    private static final String MED_IMAGE = "http://pharmexdm.com/data/uploads/2017/02/Medicine.png";
    private static final String MED_URL = "https://www.netmeds.com/";
    private static ArrayList<MedObj> MED_LIST;
    private static ArrayList<ProductModal> LOCAL_LIST;
    private static final String LOCAL_IMAGE = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRQnW0-m3tWdp-vaPaYrTmmHsz9-rPxbjm0Y1MchX8SEcS7HGaW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        pb = findViewById(R.id.pb);
        queue = Volley.newRequestQueue(this);
        totalPrice = 0;
        context = this;

        fetchFoodList();
        fetchMedList();
        fetchLocalDb();
        
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");

        detector = new TextRecognizer.Builder(getApplicationContext()).build();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
                //searchFood(((EditText)findViewById(R.id.ed_auto_correct)).getText().toString());
                //searchMed(((EditText)findViewById(R.id.ed_auto_correct)).getText().toString());
                totalPrice = 0;
                //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            }
        });
    }

    private static void fetchLocalDb() {
        SQLiteDatabase db = context.openOrCreateDatabase("MyDB", Context.MODE_PRIVATE, null);
        Cursor cursor;
        LOCAL_LIST = new ArrayList<>();
        try {
            cursor = db.rawQuery("SELECT * FROM MYPROD", null);
            while(cursor.moveToNext()){
                LOCAL_LIST.add(new ProductModal(cursor.getString(0), cursor.getString(2), cursor.getString(3), cursor.getDouble(1)));
            }
            cursor.close();
        } catch (Exception e){

        }
        for (ProductModal modal : LOCAL_LIST){
            Log.d("KyaChalRaHai", modal.getProd()+" "+modal.getPrice()+" "+modal.getLink());
        }
    }

    private void fetchFoodList() {
        FOOD_LIST = new HashMap<>();
        try {
            InputStream inputStream = getAssets().open("items_price.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while((line = br.readLine()) != null){
                FOOD_LIST.put(line.split(",")[0], Double.parseDouble(line.split(",")[1])/100);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchMedList() {
        MED_LIST = new ArrayList<>();
        try {
            InputStream inputStream = getAssets().open("medData.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while((line = br.readLine()) != null){
                String[] item = line.split(",");
                MED_LIST.add(new MedObj(item[0], item[2], MED_IMAGE, MED_URL, Double.parseDouble(item[1])));
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePicture();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    public static Bitmap rotate(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try{
                Bitmap bitmap = rotate(decodeBitmapUri(this, imageUri));
                if (detector.isOperational() && bitmap != null){
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> textBlocks = detector.detect(frame);
                    //String blocks = "", lines = "", words = "";
                    ArrayList<String> list = new ArrayList<>();
                    for (int index = 0 ; index < textBlocks.size(); index++){
                        TextBlock tblock = textBlocks.valueAt(index);
                        //blocks = blocks + tblock.getValue() + "\n";
                        for (Text line : tblock.getComponents()){
                            list.add(line.getValue());
                            //lines = lines + line.getValue() + "\n";
                            //for (Text element : line.getComponents()){
                            //    words = words + element.getValue()+", ";
                            //}
                        }
                    }
                    if (textBlocks.size() == 0){
                        Toast.makeText(this, "Scan Failed!", Toast.LENGTH_SHORT).show();
                    } else {
                        makeNetworkCalls(list);
                    }
                } else {
                    Toast.makeText(this, "Could not set up Detector", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 101 && resultCode == RESULT_OK){
            int index = data.getIntExtra("pos", 0);
            BuyhatkeResults res = buyhatkeResults.get(posInFocus)[index];
            totalPrice -= prodList.get(posInFocus).getPrice();
            prodList.get(posInFocus).setProd(res.getProd());
            prodList.get(posInFocus).setImage(res.getImage());
            prodList.get(posInFocus).setLink(res.getLink());
            prodList.get(posInFocus).setPrice(res.getPrice());
            setupRecyclerView(prodList);
            totalPrice += res.getPrice();
            ((TextView)findViewById(R.id.tv_total)).setText("Total Price: \u20B9"+totalPrice);
        }
    }

    private void makeNetworkCalls(final ArrayList<String> list) {
        pd.show();
        prodList = new ArrayList<>(list.size());
        buyhatkeResults = new ArrayList<>();
        pb.setVisibility(View.VISIBLE);
        categorize(list.get(0), list);
        //callVolley(list.get(0), list, list.size());
    }

    private boolean searchLocal(String prod){
        boolean flag = false;
        ArrayList<BuyhatkeResults> tempBuyhatke = new ArrayList<>();
        for (ProductModal modal : LOCAL_LIST){
            if (modal.getProd().toLowerCase().contains(prod.toLowerCase())){
                flag = true;
                tempBuyhatke.add(new BuyhatkeResults(modal.getProd(), modal.getPrice(), modal.getLink(), LOCAL_IMAGE, modal.getSiteName()));
            }
        }
        if (flag){
            buyhatkeResults.add(tempBuyhatke.toArray(new BuyhatkeResults[tempBuyhatke.size()]));
            prodList.add(new ProductModal(tempBuyhatke.get(0).getProd(), tempBuyhatke.get(0).getPrice(), tempBuyhatke.get(0).getLink(), tempBuyhatke.get(0).getImage(), prod, tempBuyhatke.get(0).getSiteName(), tempBuyhatke.size()));
            totalPrice += tempBuyhatke.get(0).getPrice();
        }
        return flag;
    }

    private boolean searchFood(String food) {
        boolean flag = false;
        ArrayList<BuyhatkeResults> tempBuyhatke = new ArrayList<>();
        for (String key : FOOD_LIST.keySet()) {
            if (key.toLowerCase().contains(food.toLowerCase())) {
                flag = true;
                tempBuyhatke.add(new BuyhatkeResults(key, FOOD_LIST.get(key), SWIGGY_URL, FOOD_IMAGE, "Swiggy"));
            }
        }
        if (flag){
            buyhatkeResults.add(tempBuyhatke.toArray(new BuyhatkeResults[tempBuyhatke.size()]));
            prodList.add(new ProductModal(tempBuyhatke.get(0).getProd(), tempBuyhatke.get(0).getPrice(), tempBuyhatke.get(0).getLink(), tempBuyhatke.get(0).getImage(), food, tempBuyhatke.get(0).getSiteName(), tempBuyhatke.size()));
            totalPrice += tempBuyhatke.get(0).getPrice();
        }
        return flag;
    }

    private boolean searchMed(String med) {
        boolean flag = false;
        ArrayList<BuyhatkeResults> tempBuyhatke = new ArrayList<>();
        for (MedObj obj : MED_LIST){
            if (obj.getName().toLowerCase().contains(med.toLowerCase())) {
                tempBuyhatke.add(new BuyhatkeResults(obj.getName(), obj.getPrice(), obj.getUrl(), obj.getImage(), "NetMeds"));
                flag = true;
            }
        }
        if (flag){
            buyhatkeResults.add(tempBuyhatke.toArray(new BuyhatkeResults[tempBuyhatke.size()]));
            prodList.add(new ProductModal(tempBuyhatke.get(0).getProd(), tempBuyhatke.get(0).getPrice(), tempBuyhatke.get(0).getLink(), tempBuyhatke.get(0).getImage(), med, tempBuyhatke.get(0).getSiteName(), tempBuyhatke.size()));
            totalPrice += tempBuyhatke.get(0).getPrice();
        }
        return flag;
    }

    private void categorize(String prod, ArrayList<String> list){
        if (searchLocal(prod)){
            setupRecyclerView(prodList);
            pb.setVisibility(View.GONE);
            pd.dismiss();
            list.remove(0);
            if (list.size() > 0)
                categorize(list.get(0), list);
        } else if (searchMed(prod)){
          setupRecyclerView(prodList);
          pb.setVisibility(View.GONE);
          pd.dismiss();
          list.remove(0);
          if (list.size() > 0)
              categorize(list.get(0), list);
        } else if (searchFood(prod)){
            setupRecyclerView(prodList);
            pb.setVisibility(View.GONE);
            pd.dismiss();
            list.remove(0);
            if (list.size() > 0) {
                categorize(list.get(0), list);
            }
        } else {
            pb.setVisibility(View.VISIBLE);
            callVolley(list.get(0), list);
        }
    }

    private void callVolley(final String prod, final ArrayList<String> list){
        StringRequest request = new StringRequest(Request.Method.GET, getSearchApi(prod.replace(" ", "%20")), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                pd.dismiss();
                pb.setVisibility(View.GONE);
                Log.d("Response: ", response);
                Gson gson = new Gson();
                BuyhatkeResults[] oneResult = gson.fromJson(String.valueOf(response), BuyhatkeResults[].class);
                buyhatkeResults.add(oneResult);
                if (oneResult.length > 0){
                    BuyhatkeResults temp = oneResult[oneResult.length/4];
                    totalPrice += temp.getPrice();
                    prodList.add(new ProductModal(temp.getProd(), temp.getPrice(), temp.getLink(), temp.getImage(), prod, temp.getSiteName(), oneResult.length));
                } else {
                    prodList.add(new ProductModal("Product Not Found", 0.0,"",EMPTY_IMAGE, prod, "Nowhere", 0));
                }
                setupRecyclerView(prodList);
                list.remove(0);
                if (list.size() > 0) {
                    pb.setVisibility(View.VISIBLE);
                    categorize(list.get(0), list);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VolleyError", ""+error.networkResponse);
                //Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                //pb.setVisibility(View.GONE);
                callVolley(list.get(0), list);
            }
        });
        queue.add(request);
    }

    public static void allowEdit(final int pos){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_edit);
        final EditText ed = dialog.findViewById(R.id.dialog_et);
        ed.setText(prodList.get(pos).getOriginalText());
        ed.setSelection(prodList.get(pos).getOriginalText().length());
        dialog.findViewById(R.id.dialog_btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalPrice -= prodList.get(pos).getPrice();
                pd.show();
                updateItem(pos, ed.getText().toString());
                dialog.dismiss();
            }
        });
        dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    private static boolean searchLocalInUpdate(int pos, String prod){
        boolean flag = false;
        ArrayList<BuyhatkeResults> tempBuyhatke = new ArrayList<>();
        for (ProductModal modal : LOCAL_LIST){
            if (modal.getProd().toLowerCase().contains(prod)){
                flag = true;
                tempBuyhatke.add(new BuyhatkeResults(modal.getProd(), modal.getPrice(), modal.getLink(), LOCAL_IMAGE, modal.getSiteName()));
            }
        }
        if (flag){
            buyhatkeResults.set(pos, tempBuyhatke.toArray(new BuyhatkeResults[tempBuyhatke.size()]));
            prodList.set(pos, new ProductModal(tempBuyhatke.get(0).getProd(), tempBuyhatke.get(0).getPrice(), tempBuyhatke.get(0).getLink(), tempBuyhatke.get(0).getImage(), prod, tempBuyhatke.get(0).getSiteName(), tempBuyhatke.size()));
            totalPrice += tempBuyhatke.get(0).getPrice();
        }
        return flag;
    }

    private static boolean searchFoodInUpdate(int pos, String food) {
        boolean flag = false;
        ArrayList<BuyhatkeResults> tempBuyhatke = new ArrayList<>();
        for (String key : FOOD_LIST.keySet()) {
            if (key.toLowerCase().contains(food.toLowerCase())) {
                flag = true;
                tempBuyhatke.add(new BuyhatkeResults(key, FOOD_LIST.get(key), SWIGGY_URL, FOOD_IMAGE, "Swiggy"));
            }
        }
        if (flag){
            buyhatkeResults.set(pos, tempBuyhatke.toArray(new BuyhatkeResults[tempBuyhatke.size()]));
            prodList.set(pos, new ProductModal(tempBuyhatke.get(0).getProd(), tempBuyhatke.get(0).getPrice(), tempBuyhatke.get(0).getLink(), tempBuyhatke.get(0).getImage(), food, tempBuyhatke.get(0).getSiteName(), tempBuyhatke.size()));
            totalPrice += tempBuyhatke.get(0).getPrice();
        }
        return flag;
    }

    private static boolean searchMedInUpdate(int pos, String med) {
        boolean flag = false;
        ArrayList<BuyhatkeResults> tempBuyhatke = new ArrayList<>();
        for (MedObj obj : MED_LIST){
            if (obj.getName().toLowerCase().contains(med.toLowerCase())) {
                tempBuyhatke.add(new BuyhatkeResults(obj.getName(), obj.getPrice(), obj.getUrl(), obj.getImage(), "NetMeds"));
                flag = true;
            }
        }
        if (flag){
            buyhatkeResults.set(pos, tempBuyhatke.toArray(new BuyhatkeResults[tempBuyhatke.size()]));
            prodList.set(pos, new ProductModal(tempBuyhatke.get(0).getProd(), tempBuyhatke.get(0).getPrice(), tempBuyhatke.get(0).getLink(), tempBuyhatke.get(0).getImage(), med, tempBuyhatke.get(0).getSiteName(), tempBuyhatke.size()));
            totalPrice += tempBuyhatke.get(0).getPrice();
        }
        return flag;
    }

    public static void updateItem(final int pos,final String prod) {
        fetchLocalDb();
        if (searchLocalInUpdate(pos, prod)) {
            setupRecyclerView(prodList);
            pd.dismiss();
            pb.setVisibility(View.GONE);
        } else if (searchMedInUpdate(pos, prod)) {
            setupRecyclerView(prodList);
            pd.dismiss();
            pb.setVisibility(View.GONE);
        } else if (searchFoodInUpdate(pos, prod)) {
            setupRecyclerView(prodList);
            pd.dismiss();
            pb.setVisibility(View.GONE);
        } else {
            StringRequest request = new StringRequest(Request.Method.GET, getSearchApi(prod.replace(" ", "%20")), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("Response: ", response);
                    Gson gson = new Gson();
                    BuyhatkeResults[] oneResult = gson.fromJson(String.valueOf(response), BuyhatkeResults[].class);
                    buyhatkeResults.set(pos, oneResult);
                    if (oneResult.length > 0){
                        BuyhatkeResults temp = oneResult[oneResult.length/4];
                        totalPrice += temp.getPrice();
                        prodList.set(pos, new ProductModal(temp.getProd(), temp.getPrice(), temp.getLink(), temp.getImage(), prod, temp.getSiteName(), oneResult.length));
                    } else {
                        prodList.set(pos, new ProductModal("Product Not Found", 0.0,"",EMPTY_IMAGE, prod, "Nowhere", 0));
                    }
                    setupRecyclerView(prodList);
                    pd.dismiss();
                    pb.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("VolleyError", ""+error.networkResponse);
                    updateItem(pos, prod);
                }
            });
            queue.add(request);
        }

    }

    public static void openProductOptionActivity(int pos){
        if (buyhatkeResults.get(pos) != null && buyhatkeResults.get(pos).length > 0) {
            posInFocus = pos;
            Intent intent = new Intent(context, ProductOptionsActivity.class);
            intent.putExtra("pos", pos);
            context.startActivityForResult(intent, 101);
        } else Toast.makeText(context, "No option available!", Toast.LENGTH_SHORT).show();
    }

    private static void setupRecyclerView(ArrayList<ProductModal> prodList) {
        recyclerView = (RecyclerView) context.findViewById(R.id.rl);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new ResultAdapter(prodList, context));
        ((TextView)context.findViewById(R.id.tv_total)).setText("Total Estimated Price: \u20B9"+totalPrice);
    }

    static String getSearchApi(String searchQuery){
        return "https://compare.buyhatke.com/searchEngine2.php?app_id=836312&app_auth=906149708&searchQuery="+searchQuery+"&platform=android&clientId=a08419f51587553b";
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

}
