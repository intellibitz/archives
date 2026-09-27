package intellibitz.intellidroid.company;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.R.drawable;
import intellibitz.intellidroid.bean.Company;
import intellibitz.intellidroid.bean.Company;

import java.lang.reflect.Field;
import java.util.List;


public class CompanyListAdapter extends
        BaseAdapter {
    List<Company> companies;
    LayoutInflater inflater;
    private Context context;

    public CompanyListAdapter(Context context, List<Company> companies) {
        super();
        this.context = context;
        this.companies = companies;
        inflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private int getResId(String drawableName) {

        try {
            Class<drawable> res = drawable.class;
            Field field = res.getField(drawableName);
            int drawableId = field.getInt(null);
            return drawableId;
        } catch (Exception e) {
            Log.e("CompanyCodePicker", "Failure to get drawable id.", e);
        }
        return -1;
    }

    @Override
    public int getCount() {
        return companies.size();
    }

    @Override
    public Object getItem(int arg0) {
        return companies.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return companies.get(arg0).getTypeCode().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cellView = convertView;
        Cell cell;
        Company company = companies.get(position);

        if (convertView == null) {
            cell = new Cell();
            cellView = inflater.inflate(R.layout.row, null);
            cell.tvCode = (TextView) cellView.findViewById(R.id.code);
            cell.textView = (TextView) cellView.findViewById(R.id.row_title);
//			cell.imageView = (ImageView) cellView.findViewById(R.id.row_icon);
            cellView.setTag(cell);
        } else {
            cell = (Cell) cellView.getTag();
        }

        cell.tvCode.setText(company.getTypeCode());
        cell.textView.setText(company.getType());

/*
        String drawableName = "flag_"
				+ companyName.getTypeCode().toLowerCase(Locale.ENGLISH);
		cell.imageView.setImageResource(getResId(drawableName));
*/
        return cellView;
    }

    static class Cell {
        public TextView tvCode;
        public TextView textView;
        public ImageView imageView;
    }

}