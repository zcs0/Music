package com.music.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
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
import com.music.utils.MusicTimer;
import com.music.utils.MusicUtils;
import com.music.viewpagerlistener.ViewPagerOnPageChangeListener;

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
	private List<View> mListViews = new ArrayList<View>();
	private LayoutInflater mInflater;
	private MusicAdapter mAdapter;
	private ServiceManager mServiceManager;
	private Bitmap defaultArtwork;
	private RelativeLayout mBottomLayout;
	private ListView mListView;
//	private MusicPlayBroadcast mPlayBroadcast;
//	private MusicUIManager mUIm;
	private SlidingManagerFragment mSdm;
	private MusicTimer mMusicTimer;
	private String TAG = "MusicListFragment";
	private IBaseAdapter musicAdapter;
	private BaseMusic baseMusic;
	private View contentView;
	private ListView listView;
	int layout_music = R.layout.music_list;
	/**
	 * 保存从数据库中查询的结果
	 */
	private List<BaseMusic> queryMusic;
	private TextView tv_title;
	private SPStorage pPStorage;
	private ImageButton mSearchBtn;
	
	/**
	 * 创建Adapter
	 */
	private void createAdapter() {
		if(mFrom.getValue()<=0||listView==null)return;
		tv_title.setText(mFrom.getTitle());
		new AsyncTask<Void, Void, BaseAdapter>() {
			@Override
			protected BaseAdapter doInBackground(Void... params) {
				BaseAdapter adapter2 = null;
				switch (mFrom) {
				case START_FROM_LOCAL:// 我的音乐
					queryMusic = MusicUtils.queryMusic(mActivity, mFrom);
					Collections.sort(queryMusic, new ListComparator());//排序后显示
					musicAdapter = new MusicAdapter(mActivity, mServiceManager, queryMusic);
					adapter2 = musicAdapter;
					break;
				case START_FROM_FAVORITE://我的最爱
					queryMusic = MusicUtils.queryFavorite(mActivity);
					Collections.sort(queryMusic, new ListComparator());//排序后显示
					MusicAdapter adapter = new MusicAdapter(mActivity, mServiceManager, queryMusic);
					adapter.setData(queryMusic,mFrom.getValue());
					musicAdapter = adapter;
					adapter2 = adapter;
					break;
				case START_FROM_FOLDER://文件夹
					queryMusic = MusicUtils.queryFolder(mActivity);
					musicAdapter = new FolderBrowserAdapter(mActivity, mServiceManager, queryMusic);
					adapter2 = musicAdapter;
					break;
				case START_FROM_ARTIST://歌手
					queryMusic = MusicUtils.queryArtist(mActivity);
					musicAdapter = new ArtistBrowserAdapter(mActivity, mServiceManager, queryMusic);
					adapter2 = musicAdapter;
					break;
				case START_FROM_ALBUM:// 专辑
					queryMusic = MusicUtils.queryAlbums(mActivity);
					musicAdapter = new AlbumBrowserAdapter(mActivity, mServiceManager, queryMusic);
					adapter2 = musicAdapter;
					break;
				}
				return adapter2;
			}
			@Override
			protected void onPostExecute(BaseAdapter result) {
				if(musicAdapter!=null&&listView!=null){
					listView.setAdapter(musicAdapter);
				}
			}
		}.execute();
		
		
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
					pPStorage.setLastPlayerListType(mFrom.getValue());//保存类型（程序两次进入时，查看的列表）
					ArrayList<MusicInfo> mMusicList  = new ArrayList<MusicInfo>();
					for (BaseMusic list : mList) {
						mMusicList.add((MusicInfo)list);
					}
					mServiceManager.refreshMusicList(mMusicList);
					mServiceManager.playById(((MusicInfo)baseMusic).songId);
					pPStorage.setLastPlayerId(((MusicInfo)baseMusic).songId);//保存id
					
				}else{
					List<BaseMusic> queryMusic=null;
					if(baseMusic instanceof FolderInfo){//如果是个文件夹信息
						queryMusic = MusicUtils.queryMusic(mActivity,"", baseMusic.folderPath,MusicType.START_FROM_FOLDER);
						pPStorage.setLastPlayerMusicInfo(baseMusic.folderPath);
						System.out.println(baseMusic.folderPath);
					}else if(baseMusic instanceof ArtistInfo){//歌手
						queryMusic = MusicUtils.queryMusic(mActivity,"", ((ArtistInfo)baseMusic).artist_name,MusicType.START_FROM_ARTIST);
						pPStorage.setLastPlayerMusicInfo(((ArtistInfo)baseMusic).artist_name);
						System.out.println(((ArtistInfo)baseMusic).artist_name);
					}else if(baseMusic instanceof AlbumInfo){// 专辑
						queryMusic = MusicUtils.queryMusic(mActivity,"", ((AlbumInfo)baseMusic).album_id + "",MusicType.START_FROM_ALBUM);
						pPStorage.setLastPlayerMusicInfo(((AlbumInfo)baseMusic).album_id+"");
					}
					if(queryMusic!=null&&queryMusic.size()>0){
						musicAdapter = new MusicAdapter(mActivity, mServiceManager, queryMusic);
						listView.setAdapter(musicAdapter);
					}
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
	 * 
	 * @param activity
	 * @param manager
	 * @param from
	 * @param data
	 */
	private void show(FragmentActivity activity, UIManager manager, MusicType from,
			BaseMusic data) {
		this.mBaseMusic = data;
		this.mFrom = from;
		this.mActivity = activity;
		this.mUIManager = manager;
		mServiceManager = MusicApp.mServiceManager;
		mInflater = LayoutInflater.from(activity);
		showFragment(activity, this, R.id.rl_file_list);
	}
	/**
	 * 设置播放状态
	 */
	private void initListViewStatus() {
		try {
			mSdm.setListViewAdapter(mAdapter);
			int playState = mServiceManager.getPlayState();
			if (playState == MPS_NOFILE || playState == MPS_INVALID) {
				return;
			}
			if (playState == MPS_PLAYING) {
				mMusicTimer.startTimer();
			}
			List<MusicInfo> musicList = mAdapter.getmMusicList();
			int playingSongPosition = MusicUtils.seekPosInListById(musicList,
					mServiceManager.getCurMusicId());
			mAdapter.setPlayState(playState, playingSongPosition);
			MusicInfo music = mServiceManager.getCurMusic();
			mSdm.refreshUI(mServiceManager.position(), music.duration, music);
			mSdm.showPlay(false);
//			mUIm.refreshUI(mServiceManager.position(), music.duration, music);
//			mUIm.showPlay(false);

		} catch (Exception e) {
			Log.d(TAG, "", e);
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

	@Override
	public int createView() {
		// TODO Auto-generated method stub
		return R.layout.fragment_list_item;
	}

	@Override
	public void initView(Bundle bundle, View view) {
		mActivity = getActivity();
		pPStorage = new SPStorage(getActivity());
		mListViews.add(new TextView(mActivity));
		mListViews.add(createView2());//添加真正列表显示页
		mViewPager = super.findViewById(R.id.vp_file_list);
		mViewPager.setAdapter(new DataPagerAdapter(mListViews));
		mViewPager.setCurrentItem(1, true);
		mViewPager.setOnPageChangeListener(new ViewPagerOnPageChangeListener(
				mViewPager));
		initView();//初始化控件
		createAdapter();//创建Adapter
		initListViewStatus();
		
		SPStorage mSp = new SPStorage(mActivity);
		String mDefaultBgPath = mSp.getPath();
		Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
		if(bitmap != null) {
			view.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
		}
	}

}
