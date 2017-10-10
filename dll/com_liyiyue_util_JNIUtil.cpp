#include "main.h"
#include <iostream>
#include <windows.h>
#include <stdio.h>
#include <shlobj.h>

// �����ļ������а�
int CopyFileToClipboard(const char szFileName[])
{
    DROPFILES stDrop;
    // windows���
    HGLOBAL hGblFiles;
    // �ڴ�ָ��
    LPSTR lpData;
    stDrop.pFiles = sizeof(DROPFILES);
    stDrop.pt.x = 0;
    stDrop.pt.y = 0;
    stDrop.fNC = FALSE;
    stDrop.fWide = FALSE;
    // Ϊ��������ڴ�ռ�
    hGblFiles = GlobalAlloc(GMEM_ZEROINIT | GMEM_MOVEABLE | GMEM_DDESHARE, sizeof(DROPFILES) + strlen(szFileName) + 2);
    // �Ӿ������ڴ�ָ��
    lpData = (LPSTR)GlobalLock(hGblFiles);
    memcpy(lpData, &stDrop, sizeof(DROPFILES));
    strcpy(lpData + sizeof(DROPFILES), szFileName);
    GlobalUnlock(hGblFiles);

    // �򿪼�����
    OpenClipboard(NULL);
    // ��ռ�����
    EmptyClipboard();
    // �ļ����������
    SetClipboardData(CF_HDROP,hGblFiles);
    // �رռ�����
    CloseClipboard();
    return 1;
}

// ���������������
int SetDataToClip(void)
{
    // �򿪼�����
    if (!OpenClipboard(NULL))
    {
        return 0;
    }

    HGLOBAL hMen;
    TCHAR strText[256] = "t";

    // ����ȫ���ڴ�
    hMen = GlobalAlloc(GMEM_MOVEABLE, ((strlen(strText)+1)*sizeof(TCHAR)));

    if (!hMen)
    {
        // �رռ��а�
        CloseClipboard();
        return 0;
    }

    // �����ݿ�����ȫ���ڴ���
    // ��ס�ڴ���
    LPSTR lpStr = (LPSTR)GlobalLock(hMen);

    // �ڴ渴��
    memcpy(lpStr, strText, ((strlen(strText))*sizeof(TCHAR)));
    // �ַ�������
    lpStr[strlen(strText)] = (TCHAR)0;
    // �ͷ���
    GlobalUnlock(hMen);

    // ���ڴ��е����ݷŵ����а���
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
