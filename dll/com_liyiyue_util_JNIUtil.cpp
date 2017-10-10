#include "main.h"
#include <iostream>
#include <windows.h>
#include <stdio.h>
#include <shlobj.h>

// 复制文件到剪切板
int CopyFileToClipboard(const char szFileName[])
{
    DROPFILES stDrop;
    // windows句柄
    HGLOBAL hGblFiles;
    // 内存指针
    LPSTR lpData;
    stDrop.pFiles = sizeof(DROPFILES);
    stDrop.pt.x = 0;
    stDrop.pt.y = 0;
    stDrop.fNC = FALSE;
    stDrop.fWide = FALSE;
    // 为句柄分配内存空间
    hGblFiles = GlobalAlloc(GMEM_ZEROINIT | GMEM_MOVEABLE | GMEM_DDESHARE, sizeof(DROPFILES) + strlen(szFileName) + 2);
    // 从句柄获得内存指针
    lpData = (LPSTR)GlobalLock(hGblFiles);
    memcpy(lpData, &stDrop, sizeof(DROPFILES));
    strcpy(lpData + sizeof(DROPFILES), szFileName);
    GlobalUnlock(hGblFiles);

    // 打开剪贴板
    OpenClipboard(NULL);
    // 清空剪贴板
    EmptyClipboard();
    // 文件放入剪贴板
    SetClipboardData(CF_HDROP,hGblFiles);
    // 关闭剪贴板
    CloseClipboard();
    return 1;
}

// 向剪贴板设置数据
int SetDataToClip(void)
{
    // 打开剪贴板
    if (!OpenClipboard(NULL))
    {
        return 0;
    }

    HGLOBAL hMen;
    TCHAR strText[256] = "t";

    // 分配全局内存
    hMen = GlobalAlloc(GMEM_MOVEABLE, ((strlen(strText)+1)*sizeof(TCHAR)));

    if (!hMen)
    {
        // 关闭剪切板
        CloseClipboard();
        return 0;
    }

    // 把数据拷贝考全局内存中
    // 锁住内存区
    LPSTR lpStr = (LPSTR)GlobalLock(hMen);

    // 内存复制
    memcpy(lpStr, strText, ((strlen(strText))*sizeof(TCHAR)));
    // 字符结束符
    lpStr[strlen(strText)] = (TCHAR)0;
    // 释放锁
    GlobalUnlock(hMen);

    // 把内存中的数据放到剪切板上
    SetClipboardData(CF_TEXT, hMen);
    SetClipboardData(CF_BITMAP, hMen);
    CloseClipboard();

    return 1;
}

char * JStringToCharArray(JNIEnv * pJNIEnv, jstring jstr)
{
    jsize len = pJNIEnv->GetStringLength( jstr );
    const jchar * jcstr = pJNIEnv->GetStringChars( jstr, NULL );

    int size = 0;
    char * str = ( char * )malloc( len * 2 + 1 );
    if ( (size = WideCharToMultiByte( CP_ACP, 0, LPCWSTR( jcstr ), len, str, len * 2 + 1, NULL, NULL ) ) == 0 )
        return NULL;

    pJNIEnv->ReleaseStringChars( jstr, jcstr );

    str[ size ] = 0;
    return str;
}

JNIEXPORT jboolean JNICALL Java_com_liyiyue_util_JNIUtil_setGifToClipBoard(JNIEnv* env, jclass obj, jstring jstr)
{
//    const char *str = env->GetStringUTFChars(jstr, NULL);
    const char *str = JStringToCharArray(env, jstr);
	if(CopyFileToClipboard(str))
	{
		if(SetDataToClip())
		{
//			env->ReleaseStringUTFChars(jstr, str);
			return 1;
		}
	}
//	env->ReleaseStringUTFChars(jstr, str);
	return 1;
}
