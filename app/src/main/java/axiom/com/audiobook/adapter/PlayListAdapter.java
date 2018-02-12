package axiom.com.audiobook.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import axiom.com.audiobook.R;
import butterknife.ButterKnife;

public class PlayListAdapter extends ArrayAdapter<String> {

    private ArrayList<String> mChapters;
    private Context mContext;
    private int mSelectedId = -1;

    public PlayListAdapter(Context context, int textViewResourceId, ArrayList<String> chapters) {
        super(context, R.layout.play_list_text_view, chapters);
        mContext = context;
        mChapters = chapters;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.play_list_text_view, null);
            ButterKnife.bind(this, view);
        }
        String chapterName = mChapters.get(position);

        TextView chapterNameView = (TextView) view.findViewById(R.id.tv);
        chapterNameView.setText(chapterName);

        if (position == mSelectedId) {
            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.translucent_color));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent));
        }
        return view;
    }

    public void chapterSelection(int selectedId) {
        mSelectedId = selectedId;
    }
}
