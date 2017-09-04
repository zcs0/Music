package com.music.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.MusicListSearchActivity;
import com.music.adapter.AlbumBrowserAdapter;
import com.music.adapter.ArtistBrowserAdapter;
import com.music.adapter.DataPagerAdapter;
import com.music.adapter.FolderBrowserAdapter;
import com.music.adapter.IBaseAdapter;
import com.music.adapter.MusicAdapter;
import com.music.model.AlbumInfo;
import com.music.model.ArtistInfo;
import com.music.model.BaseMusic;
import com.music.model.FolderInfo;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;
import com.music.uimanager.UIManager;
import com.music.utils.ListComparator;
import com.music.utils.MusicUtils;
import com.music.view.QuickLocationRightView;
import com.music.viewpagerlistener.ViewPagerOnPageChangeListener;
import com.z.utils.LogUtils;

/**
 * @ClassName: MusicListFragment.java
 * @author zcs
 * @version V1.0
 * @Date 2015年12月7日 下午5:42:37
 * @Description: 用来展示音乐的列表
 */
public class MusicListFragment extends MusicFragment implements IConstants {
	private IConstants.MusicType mFrom=IConstants.MusicType.START_FROM_ARTIST;
	private UIManager mUIManager;
	private BaseMusic mBaseMusic;
//	private View mView;
	private ViewPager mViewPager;
	private List<View> mAdaList = new ArrayList<View>();
	private LayoutInflater mInflater;
//	private MusicAdapter mAdapter;
	private ServiceManager mServiceManager;
	private Bitmap defaultArtwork;
	private RelativeLayout mBottomLayout;
	private ListView mListView;
//	private SlidingManagerFragment mSdm;
//	private MusicTimer mMusicTimer;
	private String TAG = "MusicListFragment";
	private IBaseAdapter musicAdapter;
	private BaseMusic baseMusic;
	private View contentView;
	private ListView listView;
	int layout_music = R.layout.music_list;
	private String musicInfo="";//保存最后一播放的音乐相关信息
	/**
	 * 保存从数据库中查询的结果
	 */
	private List<BaseMusic> queryMusic;
	private TextView tv_title;
	private SPStorage pPStorage;
	private ImageButton mSearchBtn;
	private QuickLocationRightView quickBar;
	@Override
	public int createView() {
		return R.layout.fragment_list_item;
	}

	@Override
	public void initView(Bundle bundle, View view) {
		mActivity = getActivity();
		pPStorage = MusicApp.spSD;//new SPStorage(getActivity());
		MusicUtils.openCache = true;
		mAdaList.add(new TextView(mActivity));
		mAdaList.add(createView2());//添加真正列表显示页
		mViewPager = super.findViewById(R.id.vp_file_list);
		mViewPager.setAdapter(new DataPagerAdapter(mAdaList));
		mViewPager.setCurrentItem(1, true);
		mViewPager.setOnPageChangeListener(new ViewPagerOnPageChangeListener(
				mViewPager));
		initView();//初始化控件
		quickBar = (QuickLocationRightView) findViewById(R.id.rightCharacterListView);
		initData(null);//初始化数据，创建Adapter
//		initListViewStatus();
		
		SPStorage mSp = MusicApp.spSD;;
		String mDefaultBgPath = mSp.getPath();
		Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
		if(bitmap != null) {
			view.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
		}
	}
	/**
	 * 初始化数据
	 * @param data 为空时为大分类，不为空时，为小分类
	 */
	private void initData(BaseMusic data) {
		showLoadingDialog(null);
		if(mFrom.getCode()<=0||listView==null)return;
		tv_title.setText(mFrom.getTitle());
		new AsyncTask<BaseMusic, Void, IBaseAdapter>() {
			@Override
			protected IBaseAdapter doInBackground(BaseMusic... params) {
				if(params[0]==null){
					return createAdapter(mFrom);
				}else{
					return createMusicAdapter(params[0]);
				}
			}
			@Override
			protected void onPostExecute(IBaseAdapter result) {
				dismissLoadingDialog();
				if(result!=null&&listView!=null){
					musicAdapter = result;
					listView.setAdapter(musicAdapter);
					if(musicAdapter.getCount()<5){
						quickBar.setVisibility(View.GONE);
					}else{
						quickBar.setVisibility(View.VISIBLE);
						quickBar.setListView(listView);
					}
				}
			}
		}.execute(data);
		
		
	}
	
	private View createView2(){
		contentView = contentView!=null?contentView:View.inflate(mActivity, layout_music , null);
		return contentView;
	}
	@Override
	public <T extends View> T findViewById(int layoutId) {
		// TODO Auto-generated method stub
		return (T) contentView.findViewById(layoutId);
	}
	private void initView(){
		mSearchBtn = (ImageButton) findViewById(R.id.searchBtn);//搜索
		listView = findViewById(R.id.music_list_view);
		tv_title = findViewById(R.id.tv_title);
		int playState = mServiceManager.getPlayState();
		MusicInfo music = mServiceManager.getCurMusic();
		if (playState == MPS_NOFILE || playState == MPS_INVALID) {
			//return;
		}
		
		//返回上一页
		findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				backStack();
			}
		});
		//点击条目时的事件
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<BaseMusic> mList = musicAdapter.getMList();
				BaseMusic baseMusic = mList.get(position);
				//BaseMusic baseMusic = queryMusic.get(position);
				if(baseMusic instanceof MusicInfo){//如果是一个可播放的文件
					pPStorage.setLastPlayerListType(mFrom.getCode());//保存类型（程序两次进入时，查看的列表）
					pPStorage.setLastPlayerMusicInfo(musicInfo);//保存路径
					ArrayList<MusicInfo> mMusicList  = new ArrayList<MusicInfo>();
					for (BaseMusic list : mList) {
						mMusicList.add((MusicInfo)list);
					}
					mServiceManager.refreshMusicList(mMusicList);
					mServiceManager.playById(((MusicInfo)baseMusic)._id);
					pPStorage.setLastPlayerId(((MusicInfo)baseMusic)._id);//保存id
					if(musicAdapter!=null)
						musicAdapter.notifyDataSetChanged();
				}else{
					initData(baseMusic);
				}
			}

		});
		//搜索按钮
		mSearchBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mActivity.startActivity(new Intent(mActivity,
						MusicListSearchActivity.class));
			}
		});
	}
	/**
	 * 点击小分类
	 * @param baseMusic
	 * @return
	 */
	private IBaseAdapter createMusicAdapter(BaseMusic baseMusic) {
		List<BaseMusic> queryMusic=null;
		if(baseMusic instanceof FolderInfo){//如果点击的是个文件夹信息
			queryMusic = MusicUtils.queryMusicByFolder(getActivity(),baseMusic.folderPath);
			if(queryMusic==null||queryMusic.size()<=0){
				MusicUtils.deleteFolder(getActivity(),baseMusic);
				return null;
			}
			musicInfo = baseMusic.folderPath;
		}else if(baseMusic instanceof ArtistInfo){//歌手
			queryMusic = MusicUtils.queryMusicByArtist(getActivity(),((ArtistInfo) baseMusic).artist_name);
			if(queryMusic==null||queryMusic.size()<=0){
				ArtistInfo info = (ArtistInfo)baseMusic;
				MusicUtils.deleteArtist(getActivity(),info);
				return null;
			}
			musicInfo = ((ArtistInfo)baseMusic).artist_name;
		}else if(baseMusic instanceof AlbumInfo){// 专辑
			queryMusic = MusicUtils.queryMusiceAlbums(getActivity(),((AlbumInfo) baseMusic).album_id);
			if(queryMusic==null||queryMusic.size()<=0){
				AlbumInfo info = (AlbumInfo)baseMusic;
				MusicUtils.deleteAlbum(getActivity(),info);
				return null;
			}
			musicInfo = ((AlbumInfo)baseMusic).album_id+"";
		}
//		if(mFrom!=IConstants.MusicType.START_FROM_LOCAL){//非音乐的需要重新排序
//			Collections.sort(queryMusic, new ListComparator(mFrom));//排序后显示
//		}
		if(queryMusic!=null&&queryMusic.size()>0){
//			MusicUtils.sort(queryMusic, MusicType.START_FROM_LOCAL);
//			Collections.sort(queryMusic, new ListComparator(MusicType.START_FROM_LOCAL));//排序后显示
			List<String> listString = new ArrayList<String>();
			for (BaseMusic list : queryMusic) {
				String title = list.getTitle();
				if(title==null||title.isEmpty())continue;
					listString.add(title);
			}
			quickBar.setData(listString);
			musicAdapter = new MusicAdapter(mActivity, mServiceManager);
			musicAdapter.setData(queryMusic);
		}
		return musicAdapter;
	}
	/**
	 * 点击大分类信息
	 * @param mFrom
	 * @return
	 */
	private IBaseAdapter createAdapter(IConstants.MusicType mFrom){
		IBaseAdapter adapter = null;
		queryMusic = MusicUtils.queryByType(getActivity(), mFrom);
		switch (mFrom) {
		case START_FROM_LOCAL:// 我的音乐
			adapter = new MusicAdapter(mActivity, mServiceManager);
			break;
		case START_FROM_FAVORITE://我的最爱
			adapter = new MusicAdapter(mActivity, mServiceManager);
			break;
		case START_FROM_FOLDER://文件夹
			adapter = new FolderBrowserAdapter(mActivity, mServiceManager);
			break;
		case START_FROM_ARTIST://歌手
			adapter = new ArtistBrowserAdapter(mActivity, mServiceManager);
			break;
		case START_FROM_ALBUM:// 专辑
//			queryMusic = MusicUtils.queryAlbums(mActivity);
			adapter = new AlbumBrowserAdapter(mActivity, mServiceManager);
			break;
		}
//		MusicUtils.sort(queryMusic,mFrom);//排序后显示
		if(mFrom!=IConstants.MusicType.START_FROM_LOCAL){//非音乐的需要重新排序
			Collections.sort(queryMusic, new ListComparator(mFrom));//排序后显示
		}
		List<String> listString = new ArrayList<String>();
		for (BaseMusic list : queryMusic) {
			String title = list.getTitle();
			if(title==null||title.isEmpty())continue;
				listString.add(title);
		}
		quickBar.setData(listString);
		adapter.setData(queryMusic, mFrom);
		return adapter;
	}
	/**
	 * 汉语拼音转换工具
	 *
	 * @param chinese
	 * @return
	 */
//	private String converterToPinYin(String chinese) {
//		String pinyinString = "";
//		char[] charArray = chinese.toCharArray();
//		// 根据需要定制输出格式，我用默认的即可
//		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
//		try {
//			// 遍历数组，ASC码大于128进行转换
//			for (int i = 0; i < charArray.length; i++) {
//				if (charArray[i] > 128) {
//					// charAt(0)取出首字母
//					if (charArray[i] >= 0x4e00 && charArray[i] <= 0x9fa5) { // 判断是否中文
//						pinyinString += PinyinHelper.toHanyuPinyinStringArray(
//								charArray[i], defaultFormat)[0].charAt(0);
//					} else { // 不是中文的打上未知，所以无法处理韩文日本等等其他文字
//						pinyinString += "?";
//					}
//				} else {
//					pinyinString += charArray[i];
//				}
//			}
//			return pinyinString;
//		} catch (BadHanyuPinyinOutputFormatCombination e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	/**
	 * 设置播放状态
	 */
	private void initListViewStatus() {
		try {
//			mSdm.setListViewAdapter(mAdapter);
//			int playState = mServiceManager.getPlayState();
//			if (playState == MPS_NOFILE || playState == MPS_INVALID) {
//				return;
//			}
//			if (playState == MPS_PLAYING) {
//				mMusicTimer.startTimer();
//			}
//			List<MusicInfo> musicList = mAdapter.getmMusicList();
//			int playingSongPosition = MusicUtils.seekPosInListById(musicList,
//					mServiceManager.getCurMusicId());
//			mAdapter.setPlayState(playState, playingSongPosition);
//			MusicInfo music = mServiceManager.getCurMusic();
//			mSdm.refreshUI(mServiceManager.position(), music.duration, music);
//			mSdm.showPlay(false);
//			mUIm.refreshUI(mServiceManager.position(), music.duration, music);
//			mUIm.showPlay(false);

		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.e(TAG, e.toString());
		}
	}

	/**
	 * 设置展示的数据
	 * @param baseMusic
	 */
	public void setBaseMusic(MusicType type,BaseMusic baseMusic) {
		this.mFrom = type;
		this.baseMusic = baseMusic;
		
	}
	/**
	 * 设置音乐播放管理者
	 * @param mServiceManager2
	 */
	public void setServiceManager(ServiceManager sm) {
		this.mServiceManager=sm;
		
	}

	

}
