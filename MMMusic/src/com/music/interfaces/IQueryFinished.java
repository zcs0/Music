/**
 * Copyright (c) www.longdw.com
 */
package com.music.interfaces;

import java.util.List;

import com.music.model.MusicInfo;

public interface IQueryFinished {
	
	public void onFinished(List<MusicInfo> list);

}
