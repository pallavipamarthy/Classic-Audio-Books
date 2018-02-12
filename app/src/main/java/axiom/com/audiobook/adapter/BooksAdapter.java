package axiom.com.audiobook.adapter;

import android.content.Context;
import android.content.Intent;
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
import axiom.com.audiobook.data.Book;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {
    List<Book> mBooksList;
    Context mContext;
    public BooksAdapter(Context context, ArrayList<Book> bookList){
        mContext = context;
        mBooksList = bookList;
    }

    public BooksAdapter.BookViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        mContext = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.search_list_item_layout,viewGroup,false);
        ButterKnife.bind(this,view);
        BooksAdapter.BookViewHolder viewHolder = new BooksAdapter.BookViewHolder(view);
        return viewHolder;
    }

    //create view holder with views
    //Onclick on each grid item sends an intent to launch detail activity
    class BookViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.search_list_image_view)
        ImageView mSearchListImageView;
        @BindView(R.id.book_title_text_view)
        TextView mBookTitleView;
        @BindView(R.id.author_textview)
        TextView mAuthorTextView;
        @BindView(R.id.duration_textview)
        TextView mDurationTextView;

        public BookViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    //Binding data to the views
    @Override
    public void onBindViewHolder(BooksAdapter.BookViewHolder holder, final int position) {
        holder.mBookTitleView.setText(mBooksList.get(position).getTitle());
        holder.mAuthorTextView.setText(mBooksList.get(position).getAuthorName());
        holder.mDurationTextView.setText(mBooksList.get(position).getTotalTime());
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(mContext,BookDetailActivity.class);
                intent.putExtra("book_obj",mBooksList.get(position));
                mContext.startActivity(intent);
            }
        });
        String imageUrl = mBooksList.get(position).getThumbNailUrl();
        Picasso.with(mContext).load(imageUrl).fit().placeholder(R.mipmap.default_placeholder).into(holder.mSearchListImageView);
    }

    @Override
    public int getItemCount() {
        return mBooksList.size();
    }

    //Set movie object passed from load finished to here.
    public void setData(List<Book> moviesList) {
        if(mBooksList!=null){
            mBooksList.clear();
            mBooksList = moviesList;
            notifyDataSetChanged();
        }
    }
}
