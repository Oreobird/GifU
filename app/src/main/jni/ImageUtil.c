//
// Created by zgs on 2016/12/14.
//
#include <jni.h>
#include <stdlib.h>
#include "ImageUtil.h"
#include <android/log.h>
#include <android/bitmap.h>
#include <math.h>

#define LOG_TAG "Paco.imageutil"
//#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
//#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGI(...)
#define LOGE(...)
int min(int x, int y) {
    return (x <= y) ? x : y;
}
int max(int x,int y){
    return (x >= y) ? x : y;
}
int alpha(int color) {
    return (color >> 24) & 0xFF;
}
int red(int color) {
    return (color >> 16) & 0xFF;
}
int green(int color) {
    return (color >> 8) & 0xFF;
}
int blue(int color) {
    return color & 0xFF;
}
int ARGB(int alpha, int red, int green, int blue) {
    return (alpha << 24) | (red << 16) | (green << 8) | blue;
}

#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>
#include <linux/fb.h>
#include <sys/mman.h>
inline static unsigned short int make16color(unsigned char r, unsigned char g, unsigned char b)
{
    return (
            (((r >> 3) & 31) << 11) |
            (((g >> 2) & 63) << 5)  |
            ((b >> 3) & 31)        );
}

int framebuffer_main()
{
    LOGI("framebuffer code");
    int fbfd = 0;
    struct fb_var_screeninfo vinfo;
    struct fb_fix_screeninfo finfo;
    long int screensize = 0;
    char *fbp = 0;
    int x = 0, y = 0;
    int guage_height = 20, step = 10;
    long int location = 0;
    // Open the file for reading and writing
    LOGI("framebuffer code 1");
    fbfd = open("/dev/graphics/fb0", O_RDWR);
    LOGI("framebuffer code 2");
    if (!fbfd) {
        LOGI("Error: cannot open framebuffer device.\n");
        exit(1);
    }
    LOGI("The framebuffer device was opened successfully.\n");
    // Get fixed screen information
    if (ioctl(fbfd, FBIOGET_FSCREENINFO, &finfo)) {
        LOGI("Error reading fixed information.\n");
        exit(2);
    }
    // Get variable screen information
    if (ioctl(fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
        LOGI("Error reading variable information.\n");
        exit(3);
    }
    LOGI("sizeof(unsigned short) = %d\n", sizeof(unsigned short));
    LOGI("%dx%d, %dbpp\n", vinfo.xres, vinfo.yres, vinfo.bits_per_pixel);
    LOGI("xoffset:%d, yoffset:%d, line_length: %d\n", vinfo.xoffset,
         vinfo.yoffset, finfo.line_length);
    // Figure out the size of the screen in bytes
    screensize = vinfo.xres * vinfo.yres * vinfo.bits_per_pixel / 8;
    // Map the device to memory
    fbp = (char *) mmap(0, screensize, PROT_READ | PROT_WRITE, MAP_SHARED,
                        fbfd, 0);
    if ((int) fbp == -1) {
        LOGI("Error: failed to map framebuffer device to memory.\n");
        exit(4);
    }
    LOGI("The framebuffer device was mapped to memory successfully.\n");
    //set to black color first
    memset(fbp, 0, screensize);
    //draw rectangle
    y = (vinfo.yres - guage_height) / 2 - 2; // Where we are going to put the pixel
    for (x = step - 2; x < vinfo.xres - step + 2; x++) {
        location = (x + vinfo.xoffset) * (vinfo.bits_per_pixel / 8) + (y
                                                                       + vinfo.yoffset) * finfo.line_length;
        *((unsigned short int*) (fbp + location)) = 255;
    }
    y = (vinfo.yres + guage_height) / 2 + 2; // Where we are going to put the pixel
    for (x = step - 2; x < vinfo.xres - step + 2; x++) {
        location = (x + vinfo.xoffset) * (vinfo.bits_per_pixel / 8) + (y
                                                                       + vinfo.yoffset) * finfo.line_length;
        *((unsigned short int*) (fbp + location)) = 255;
    }
    x = step - 2;
    for (y = (vinfo.yres - guage_height) / 2 - 2; y < (vinfo.yres
                                                       + guage_height) / 2 + 2; y++) {
        location = (x + vinfo.xoffset) * (vinfo.bits_per_pixel / 8) + (y
                                                                       + vinfo.yoffset) * finfo.line_length;
        *((unsigned short int*) (fbp + location)) = 255;
    }
    x = vinfo.xres - step + 2;
    for (y = (vinfo.yres - guage_height) / 2 - 2; y < (vinfo.yres
                                                       + guage_height) / 2 + 2; y++) {
        location = (x + vinfo.xoffset) * (vinfo.bits_per_pixel / 8) + (y
                                                                       + vinfo.yoffset) * finfo.line_length;
        *((unsigned short int*) (fbp + location)) = 255;
    }
    // Figure out where in memory to put the pixel
    for (x = step; x < vinfo.xres - step; x++) {
        for (y = (vinfo.yres - guage_height) / 2; y < (vinfo.yres
                                                       + guage_height) / 2; y++) {
            location = (x + vinfo.xoffset) * (vinfo.bits_per_pixel / 8) + (y
                                                                           + vinfo.yoffset) * finfo.line_length;
            if (vinfo.bits_per_pixel == 32) {
                *(fbp + location) = 100; // Some blue
                *(fbp + location + 1) = 15 + (x - 100) / 2; // A little green
                *(fbp + location + 2) = 200 - (y - 100) / 5; // A lot of red
                *(fbp + location + 3) = 0; // No transparency
            } else { //assume 16bpp
                unsigned char b = 255 * x / (vinfo.xres - step);
                unsigned char g = 255; // (x - 100)/6 A little green
                unsigned char r = 255; // A lot of red
                unsigned short int t = make16color(r, g, b);
                *((unsigned short int*) (fbp + location)) = t;
            }
        }
        //printf("x = %d, temp = %d\n", x, temp);
        //sleep to see it
        usleep(200);
    }
    //clean framebuffer
    munmap(fbp, screensize);
    close(fbfd);
    return 0;
}

int r_v_table[256],g_v_table[256],g_u_table[256],b_u_table[256],y_table[256];
int r_yv_table[256][256],b_yu_table[256][256];
int inited = 0;

void initTable()
{
    if (inited == 0)
    {
        //framebuffer_main();
        inited = 1;
        int m = 0,n=0;
        for (; m < 256; m++)
        {
            r_v_table[m] = 1634 * (m - 128);
            g_v_table[m] = 833 * (m - 128);
            g_u_table[m] = 400 * (m - 128);
            b_u_table[m] = 2066 * (m - 128);
            y_table[m] = 1192 * (m - 16);
        }
        int temp = 0;
        for (m = 0; m < 256; m++)
            for (n = 0; n < 256; n++)
            {
                temp = 1192 * (m - 16) + 1634 * (n - 128);
                if (temp < 0) temp = 0; else if (temp > 262143) temp = 262143;
                r_yv_table[m][n] = temp;

                temp = 1192 * (m - 16) + 2066 * (n - 128);
                if (temp < 0) temp = 0; else if (temp > 262143) temp = 262143;
                b_yu_table[m][n] = temp;
            }
    }
}

void Java_com_zgs_gifu_utils_ImageUtil_decodeYUV420SP(JNIEnv * env,
                                                        jobject thiz, jintArray rgb, jbyteArray buf, jint width, jint height) {
    jbyte * yuv420sp = (*env)->GetByteArrayElements(env, buf, 0);
    jint * rgbptr = (*env)->GetIntArrayElements(env, rgb, 0);

    int frameSize = width * height;
    //jint rgb[frameSize];
    //LOGI("-----------Java_com_zgs_gifu_utils_ImageUtil_decodeYUV420SP\n");
    initTable();

    int i = 0, j = 0,yp = 0;
    int uvp = 0, u = 0, v = 0;
    for (j = 0, yp = 0; j < height; j++)
    {
        uvp = frameSize + (j >> 1) * width;
        u = 0;
        v = 0;
        for (i = 0; i < width; i++, yp++)
        {
            int y = (0xff & ((int) yuv420sp[yp]));
            if (y < 0)
                y = 0;
            if ((i & 1) == 0)
            {
                v = (0xff & yuv420sp[uvp++]);
                u = (0xff & yuv420sp[uvp++]);
            }

//			int y1192 = 1192 * y;
//			int r = (y1192 + 1634 * v);
//			int g = (y1192 - 833 * v - 400 * u);
//			int b = (y1192 + 2066 * u);
            int y1192 = y_table[y];
            int r = r_yv_table[y][v];//(y1192 + r_v_table[v]);
            int g = (y1192 - g_v_table[v] - g_u_table[u]);
            int b = b_yu_table[y][u];//(y1192 + b_u_table[u]);

            //if (r < 0) r = 0; else if (r > 262143) r = 262143;
            if (g < 0) g = 0; else if (g > 262143) g = 262143;
            //if (b < 0) b = 0; else if (b > 262143) b = 262143;
//			r = (r >> 31) ? 0 : (r & 0x3ffff);
//			g = (g >> 31) ? 0 : (g & 0x3ffff);
//			b = (b >> 31) ? 0 : (b & 0x3ffff);

            rgbptr[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
        }
    }

    //jintArray result = (*env)->NewIntArray(env, frameSize);
    //(*env)->SetIntArrayRegion(env, result, 0, frameSize, rgb);

    (*env)->ReleaseByteArrayElements(env, buf, yuv420sp, 0);
    (*env)->ReleaseIntArrayElements(env, rgb, rgbptr, 0);
    return;
}

