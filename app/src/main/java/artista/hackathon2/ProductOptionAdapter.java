package artista.hackathon2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ProductOptionAdapter extends RecyclerView.Adapter<ProductOptionAdapter.ViewHolder> {

    private BuyhatkeResults[] buyhatkeResults;
    private Activity context;

    public ProductOptionAdapter(BuyhatkeResults[] buyhatkeResults, Activity context){
        this.buyhatkeResults = buyhatkeResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductOptionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_option_item, parent, false);
        return new ProductOptionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder,final int i) {
        final BuyhatkeResults result = buyhatkeResults[i];
        viewHolder.name.setText(result.getProd());
        viewHolder.price.setText("\u20B9"+result.getPrice());
        viewHolder.siteName.setText(result.getSiteName());
        viewHolder.btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = context.getIntent();
                intent.putExtra("pos", i);
                context.setResult(Activity.RESULT_OK, intent);
                context.finish();
            }
        });
        if(result.getImage() != ""){
            Picasso.with(context).load(result.getImage()).fit().centerInside().into(viewHolder.image);
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("url", result.getLink());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buyhatkeResults.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name, price, siteName;
        private ImageView image;
        private Button btnChoose;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.product_title_option);
            price = itemView.findViewById(R.id.product_price_option);
            siteName = itemView.findViewById(R.id.site_name_option);
            image = itemView.findViewById(R.id.product_image_option);
            btnChoose = itemView.findViewById(R.id.btn_choose);
        }
    }
}
