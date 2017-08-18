/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netUtil.ImageUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.music.BuildConfig;


/**
 * 一个简单的磁盘LRU位图缓存来说明如何磁盘缓存将用于位图缓存. 一个更加强大和高效的磁盘LRU缓存解决方案可以在ICS源代码中找到
 * (libcore/luni/src/main/java/libcore/io/DiskLruCache.java) 并且优选的是这个简单的实现。
 */
public class DiskLruCache {
	private static final String TAG = "DiskLruCache";
	private static final String CACHE_FILENAME_PREFIX = "";// 文件名过滤器
	private static final int MAX_REMOVALS = 4;
	private static final int INITIAL_CAPACITY = 32;
	private static final float LOAD_FACTOR = 0.75f;

	private final File mCacheDir;
	private int cacheSize = 0;
	private int cacheByteSize = 0;
	private final int maxCacheItemSize = 60; // 64 item default
	private long maxCacheByteSize = 1024 * 1024 * 5; // 5MB default
	private CompressFormat mCompressFormat = CompressFormat.JPEG;
	private int mCompressQuality = 100;

	private final Map<String, String> mLinkedHashMap = Collections
			.synchronizedMap(new LinkedHashMap<String, String>(
					INITIAL_CAPACITY, LOAD_FACTOR, true));

	/**
	 * 文件名过滤器用来标识缓存文件名具有 CACHE_FILENAME_PREFIX prepended.
	 */
	private static final FilenameFilter cacheFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			return filename.startsWith(CACHE_FILENAME_PREFIX);
		}
	};

	/**
	 * 用于获取DiskLruCache的一个实例。
	 * 
	 * @param context
	 * @param cacheDir
	 *            文件缓存路径
	 * @param maxByteSize
	 *            缓存大小
	 * @return
	 */
	public static DiskLruCache openCache(Context context, File cacheDir,
			long maxByteSize) {
		if (!cacheDir.exists()) {
			cacheDir.mkdir();
		}
		// 如果文件夹不存在
		if (cacheDir.isDirectory() && cacheDir.canWrite()
				&& Utils.getUsableSpace(cacheDir) > maxByteSize) {
			return new DiskLruCache(cacheDir, maxByteSize);
		}

		return null;
	}

	/**
	 * 构造函数不应该直接调用, instead use
	 * {@link DiskLruCache#openCache(Context, File, long)}
	 * 它创建一个DiskLruCache实例之前运行一些额外的检查
	 * 
	 * @param cacheDir
	 *            文件
	 * @param maxByteSize
	 *            缓存大小
	 */
	private DiskLruCache(File cacheDir, long maxByteSize) {
		mCacheDir = cacheDir;
		maxCacheByteSize = maxByteSize;
	}

	/**
	 * 添加一个位图到磁盘缓存。
	 * 
	 * @param key
	 *            的唯一标识符的位图。
	 * @param data
	 *            位图来存储。
	 */
	public void put(String key, Bitmap data) {
		synchronized (mLinkedHashMap) {
			if (mLinkedHashMap.get(key) == null) {
				try {
					final String file = createFilePath(mCacheDir, key);
					if (writeBitmapToFile(data, file)) {
						put(key, file);
						flushCache();
					}
				} catch (final FileNotFoundException e) {
					Log.e(TAG, "Error in put: " + e.getMessage());
				} catch (final IOException e) {
					Log.e(TAG, "Error in put: " + e.getMessage());
				}
			}
		}
	}

	private void put(String key, String file) {
		mLinkedHashMap.put(key, file);
		cacheSize = mLinkedHashMap.size();
		cacheByteSize += new File(file).length();
	}

	/**
	 * 刷新缓存，除去最早的项如果总大小超过指定的高速缓存大小。注意，这是不跟踪陈旧文件不在HashMap的高速缓冲存储器目录。
	 * 如果在磁盘缓存变化的图像和键经常然后他们可能永远不会被删除。
	 */
	private void flushCache() {
		Entry<String, String> eldestEntry;
		File eldestFile;
		long eldestFileSize;
		int count = 0;

		while (count < MAX_REMOVALS
				&& (cacheSize > maxCacheItemSize || cacheByteSize > maxCacheByteSize)) {
			eldestEntry = mLinkedHashMap.entrySet().iterator().next();
			eldestFile = new File(eldestEntry.getValue());
			eldestFileSize = eldestFile.length();
			mLinkedHashMap.remove(eldestEntry.getKey());
			eldestFile.delete();
			cacheSize = mLinkedHashMap.size();
			cacheByteSize -= eldestFileSize;
			count++;
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "flushCache - Removed cache file, " + eldestFile
						+ ", " + eldestFileSize);
			}
		}
	}

	/**
	 * 获得从磁盘缓存的图像。
	 * 
	 * @param key
	 *            对于位图中的唯一标识符
	 * @return NULL:如果未找到
	 */
	public Bitmap get(String key) {
		synchronized (mLinkedHashMap) {
			final String file = mLinkedHashMap.get(key);
			if (file != null) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "Disk cache hit");
				}
				return BitmapFactory.decodeFile(file);
			} else {
				final String existingFile = createFilePath(mCacheDir, key);
				if (new File(existingFile).exists()) {
					put(key, existingFile);
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "Disk cache hit (existing file)");
					}
					return BitmapFactory.decodeFile(existingFile);
				}
			}
			return null;
		}
	}

	/**
	 * 检查，如果一个特定的键存在于高速缓存中。
	 * 
	 * @param key
	 *            对于位图中的唯一标识符
	 * @return 否则，真若发现假
	 */
	public boolean containsKey(String key) {
		// 看看最关键的是在我们的HashMap
		if (mLinkedHashMap.containsKey(key)) {
			return true;
		}

		// 现在，检查是否有存在基于密钥的实际文件
		final String existingFile = createFilePath(mCacheDir, key);
		if (new File(existingFile).exists()) {
			// 文件中发现，其添加到HashMap中以备将来使用
			put(key, existingFile);
			return true;
		}
		return false;
	}

	/**
	 * 删除所有磁盘高速缓存条目这种情况下缓存目录
	 */
	public void clearCache() {
		DiskLruCache.clearCache(mCacheDir);
	}

	/**
	 * 移除在uniqueName子目录的应用程序缓存目录中的所有磁盘高速缓存条目。
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            独特的缓存目录的名称追加到应用程序缓存目录
	 */
	public static void clearCache(Context context, String uniqueName) {
		File cacheDir = getDiskCacheDir(context, uniqueName);
		clearCache(cacheDir);
	}

	/**
	 * 移除指定的目录中的所有磁盘高速缓存条目。这不应该被直接调用, call
	 * {@link DiskLruCache#clearCache(Context, String)} or
	 * {@link DiskLruCache#clearCache()} instead.
	 * 
	 * @param cacheDir
	 *            从删除缓存文件的目录
	 */
	private static void clearCache(File cacheDir) {
		final File[] files = cacheDir.listFiles(cacheFileFilter);
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}

	/**
	 * 获得一个可用的缓存目录（外部如果有的话，内部其他方式）。
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            独特的目录名称追加到缓存目录
	 * @return 缓存目录
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {

		// 检查介质安装或存储是内置的，如果是的话，尝试使用
		// 外部缓存目录
		// 否则使用内部缓存目录
		final String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
				|| !Utils.isExternalStorageRemovable() ? Utils
				.getExternalCacheDir(context).getPath() : context.getCacheDir()
				.getPath();

		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * 创建给定目标缓存目录和图像键恒定的缓存文件路径。
	 * 
	 * @param cacheDir
	 * @param key
	 * @return
	 */
	public static String createFilePath(File cacheDir, String key) {
		try {
			// 使用URLEncoder的，以确保我们有一个有效的文件名，一点点哈克
			// 但它会做
			// 这个例子
			String encode = URLEncoder.encode(key.replace("*", ""), "UTF-8");
			return cacheDir.getAbsolutePath() + File.separator
					+ CACHE_FILENAME_PREFIX + encode;
		} catch (final UnsupportedEncodingException e) {
			Log.e(TAG, "createFilePath - " + e);
		}

		return null;
	}

	/**
	 * 创建使用当前缓存目录和图像键恒定的缓存文件路径。 图像的键
	 * 
	 * @param key
	 * @return
	 */
	public String createFilePath(String key) {
		return createFilePath(mCacheDir, key);
	}

	/**
	 * 设定目标压缩格式和质量为写入到磁盘高速缓存的图像。
	 * 
	 * @param compressFormat
	 * @param quality
	 */
	public void setCompressParams(CompressFormat compressFormat, int quality) {
		mCompressFormat = compressFormat;
		mCompressQuality = quality;
	}

	/**
	 * 写一个位图文件. Call {@link DiskLruCache#setCompressParams(CompressFormat, int)}
	 * 先设定目标位图压缩和格式。
	 * 
	 * @param bitmap
	 * @param file
	 * @return
	 */
	private boolean writeBitmapToFile(Bitmap bitmap, String file)
			throws IOException, FileNotFoundException {

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file),
					Utils.IO_BUFFER_SIZE);
			return bitmap.compress(mCompressFormat, mCompressQuality, out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}
