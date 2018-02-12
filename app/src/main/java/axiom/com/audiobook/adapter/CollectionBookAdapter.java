package axiom.com.audiobook.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import axiom.com.audiobook.R;
import axiom.com.audiobook.activities.BookDetailActivity;
import axiom.com.audiobook.activities.GenreCatalogActivity;
import axiom.com.audiobook.data.Book;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CollectionBookAdapter extends RecyclerView.Adapter<CollectionBookAdapter.BookViewHolder> {
    private List<Book> mBooksList;
    private Context mContext;
    private int mLayoutId;

    public CollectionBookAdapter(Context context, ArrayList<Book> bookList, int layoutId) {
        mContext = context;
        mBooksList = bookList;
        mLayoutId = layoutId;
    }

    public CollectionBookAdapter.BookViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(mLayoutId, viewGroup, false);
        ButterKnife.bind(this, view);
        return new CollectionBookAdapter.BookViewHolder(view);
    }

    //create view holder with views
    //Onclick on each grid item sends an intent to launch detail activity
    class BookViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.book_title_text_view)
        TextView mBookTitleView;
        @BindView(R.id.book_image_view)
        ImageView mBookImageView;

        public BookViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.book_image_view)
        public void onClick(View v) {
            Intent intent = new Intent(mContext, BookDetailActivity.class);
            intent.putExtra("book_obj", mBooksList.get(getAdapterPosition()));
            if ((Activity) mContext instanceof GenreCatalogActivity) {
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) mContext, (View) v, "bookImage");
                mContext.startActivity(intent, options.toBundle());
            } else {
                mContext.startActivity(intent);
            }
        }
    }


    //Binding data to the views
    @Override
    public void onBindViewHolder(CollectionBookAdapter.BookViewHolder holder, int position) {
        holder.mBookTitleView.setText(mBooksList.get(position).getTitle());
        String imageUrl = mBooksList.get(position).getThumbNailUrl();
        Picasso.with(mContext).load(imageUrl).placeholder(R.mipmap.default_placeholder).fit().into(holder.mBookImageView);
    }

    @Override
    public int getItemCount() {
        return mBooksList.size();
    }

    //Set movie object passed from load finished to here.
    public void setData(List<Book> bookList) {
        mBooksList.clear();
        mBooksList = bookList;
        notifyDataSetChanged();
    }
}
