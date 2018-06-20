package artista.hackathon2;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private ArrayList<ProductModal> mSearchResults;
    private Context context;

    public ResultAdapter(ArrayList<ProductModal> searchResults, Context context){
        mSearchResults = searchResults;
        this.context = context;
    }

    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ResultAdapter.ViewHolder holder, final int position) {
        final ProductModal result = mSearchResults.get(position);
        holder.productName.setText(result.getProd());
        holder.productPrice.setText("\u20B9" + getIntegerFromDouble(result.getPrice()));
        holder.originalText.setText("Original Text: "+result.getOriginalText());
        if (result.getMoreCount() != 0) holder.tvMore.setText(result.getMoreCount()+" more product(s)");
        holder.tvSiteName.setText(result.getSiteName());

        if (TextUtils.isEmpty(result.getLink())) holder.btnBuyNow.setText("Add");

        holder.btnBuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.btnBuyNow.getText().equals("Add")){
                    Toast.makeText(context, "New Prod Add Maadi", Toast.LENGTH_SHORT).show();
                    showAddProdDialog(position, result.getOriginalText());
                } else {
                    Intent intent = new Intent(context, WebActivity.class);
                    intent.putExtra("url", result.getLink());
                    context.startActivity(intent);
                }
            }
        });

        if(result.getImage() != ""){
            Picasso.with(context).load(result.getImage()).fit().into(holder.productImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.openProductOptionActivity(position);
            }
        });

        holder.tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.allowEdit(position);
            }
        });
    }

    private void showAddProdDialog(final int position, String originalText) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_prod);
        dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.show();
        ((EditText)dialog.findViewById(R.id.dialog_ed_name)).setText(originalText);
        ((EditText)dialog.findViewById(R.id.dialog_ed_name)).setSelection(originalText.length());
        dialog.findViewById(R.id.btn_add_new_prod).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveValuesToDB(
                        position,
                        ((EditText)dialog.findViewById(R.id.dialog_ed_name)).getText().toString(),
                        ((EditText)dialog.findViewById(R.id.dialog_ed_price)).getText().toString(),
                        ((EditText)dialog.findViewById(R.id.dialog_ed_url)).getText().toString(),
                        ((EditText)dialog.findViewById(R.id.dialog_ed_siteName)).getText().toString()
                );
                dialog.dismiss();
            }
        });
    }

    private void saveValuesToDB(int pos, String name, String price, String url, String siteName) {
        SQLiteDatabase db = context.openOrCreateDatabase("MyDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS MYPROD (NAME VARCHAR(200), PRICE DOUBLE, URL VARCHAR(500), SITENAME VARCHAR(200))");
        ContentValues cv = new ContentValues();
        cv.put("NAME", name);
        cv.put("PRICE", Double.parseDouble(price));
        cv.put("URL", url);
        cv.put("SITENAME", siteName);
        db.replace("MYPROD", null, cv);
        MainActivity.updateItem(pos, name);
    }

    private int getIntegerFromDouble(double num){
        return (int)num;
    }

    @Override
    public int getItemCount() {
        return mSearchResults.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName;
        private TextView productPrice;
        private ImageView productImage;
        private TextView originalText;
        private Button btnBuyNow;
        private TextView tvEdit, tvMore, tvSiteName;

        public ViewHolder(View itemView) {
            super(itemView);
            //this.indicesToShow = indicesToShow;
            productName = (TextView) itemView.findViewById(R.id.product_title);
            productPrice = (TextView) itemView.findViewById(R.id.product_price);
            productImage = (ImageView) itemView.findViewById(R.id.product_image);
            originalText = (TextView) itemView.findViewById(R.id.original_text);
            btnBuyNow = itemView.findViewById(R.id.btn_buy_now);
            tvEdit = itemView.findViewById(R.id.tv_edit);
            tvSiteName = itemView.findViewById(R.id.site_name);
            tvMore = itemView.findViewById(R.id.more_prod);
        }
    }

}
