package com.music.dialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.music.R;
import com.music.utils.StringHelper;

/**
 * @ClassName:     FileExplorerDialog.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月26日 下午10:28:50 
 * @Description:   TODO(用一句话描述该文件做什么) 
 */
public class FileExplorerDialog extends Dialog{

	private List<String> fileList;
	private ItemClickListener itemClick;
	public FileExplorerDialog(Context context, int theme) {
		super(context, theme);
		WindowManager wm = (WindowManager) getContext() .getSystemService(Context.WINDOW_SERVICE);
	     int width = wm.getDefaultDisplay().getWidth();
	     int height = wm.getDefaultDisplay().getHeight();
		View view = View
				.inflate(context, R.layout.file_explorer_dialog, null);
		view.setMinimumWidth(width-100);
		view.setMinimumHeight(width-150);
		setContentView(view);
		ListView lvFile = (ListView) view.findViewById(R.id.lv_file_list);
		lvFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(itemClick!=null){
					itemClick.click(view, position, id);
				}
				
			}
		});
		lvFile.setAdapter(new IAdapter());
	}
	public void setOnItemClickListener(ItemClickListener itemClick){
		this.itemClick = itemClick;
	}
	public interface ItemClickListener{
		public void click(View view,int position, long id);
	}
	public void setFileList(List<String> fileList){
		Collections.sort(fileList, new Comparat());
		this.fileList = fileList;
	}
	public class Comparat implements Comparator<String>{
		char first_l, first_r;
		@Override
		public int compare(String lhs, String rhs) {
			first_l = lhs.charAt(0);
			first_r = rhs.charAt(0);
			if (StringHelper.checkType(first_l) == StringHelper.CharType.CHINESE) {
				first_l = StringHelper.getPinyinFirstLetter(first_l);
			}
			if (StringHelper.checkType(first_r) == StringHelper.CharType.CHINESE) {
				first_r = StringHelper.getPinyinFirstLetter(first_r);
			}
			if (first_l > first_r) {
				return 1;
			} else if (first_l < first_r) {
				return -1;
			} else {
				return 0;
			}
		}


	}
	
	
	String fileMain = Environment.getExternalStorageDirectory()+"/";
	class IAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileList==null?0:fileList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = View.inflate(getContext(),R.layout.files_dialog_item, null);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.tv_info);
			tv.setText(fileList.get(position));
			return convertView;
		}
	}
	
}
