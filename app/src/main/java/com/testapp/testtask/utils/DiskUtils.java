package com.testapp.testtask.utils;

import android.os.Environment;
import android.os.StatFs;

/**
 *
 * Some helper methods for FS queries.
 */
public class DiskUtils {
  private static final long GIGABYTE = 1073741824;

  /**
   * Calculates total space on disk
   * @param external  If true will query external disk, otherwise will query internal disk.
   * @return Number of gigabytes on disk.
   */
  public static long totalSpace(boolean external)
  {
    StatFs statFs = getStats(external);
    return ((statFs.getBlockCountLong()) * (statFs.getBlockSizeLong())) / GIGABYTE;
  }

  /**
   * Calculates free space on disk
   * @param external  If true will query external disk, otherwise will query internal disk.
   * @return Number of free gigabytes on disk.
   */
  public static long freeSpace(boolean external)
  {
    StatFs statFs = getStats(external);
    long availableBlocks = statFs.getAvailableBlocksLong();
    long blockSize = statFs.getBlockSizeLong();
    long freeBytes = availableBlocks * blockSize;

    return (freeBytes / GIGABYTE);
  }


  private static StatFs getStats(boolean external){
    String path;

    if (external){
      path = Environment.getExternalStorageDirectory().getAbsolutePath();
    }
    else{
      path = Environment.getRootDirectory().getAbsolutePath();
    }

    return new StatFs(path);
  }

}
