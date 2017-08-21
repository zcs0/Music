/**
 * Copyright (c) www.longdw.com
 */
package com.music.model;


/**
 * 歌词句子，是一个时间戳和一行歌词组成，如“[00.03.21.56]还记得许多年前的春天”
 * */
public class LyricSentence {

	/** 歌詞文本的开始时间戳转换为毫秒数的值，如[00.01.02.34]为62340毫秒 */
	private long startTime = 0;

	/**一句歌词的实现*/
	private long duringTime = 0;

	/** 每个时间戳对应的一行歌词文本,如“[00.03.21.56]还记得许多年前的春天”中的“还记得许多年前的春天” */
	private String contentText = "";

	private String[] intervalTime;

	public LyricSentence(long time, String text) {
		startTime = time;
		setContentText(text);
	}
	/**
	 * 本句的开始时间
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	/**
	 * 本句的文本信息
	 * @return
	 */
	public String getContentText() {
		return contentText;
	}

	public void setContentText(String contentText) {
		String line = contentText.replaceAll("<[0-9]+>", "");
		this.contentText = line;
		this.intervalTime = contentText.split("\\D+");
		//<138>忘<153>记<202>时<152>间 <204>- <203>胡<153>歌
	}
	/**
	 * 下一句的开始时间
	 * @return
	 */
	public long getDuringTime() {
		return duringTime;
	}
	/**
	 * 下一句的开始时间
	 * @param duringTime
	 */
	public void setDuringTime(long duringTime) {
		this.duringTime = duringTime;
	}
}
