package com.khoray.jigsawgame.utils;

import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.Texture;
import ohos.agp.utils.Color;
import ohos.media.image.PixelMap;
import ohos.media.image.common.ImageInfo;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

public class PicCutter {
    static public PixelMap rescale(PixelMap pm) {
        ImageInfo imginfo = pm.getImageInfo();

        int h = imginfo.size.height;
        int w = imginfo.size.width;
        int sz = Math.min(h, w);
        PixelMap.InitializationOptions options = new PixelMap.InitializationOptions();
        options.pixelFormat = imginfo.pixelFormat;
        options.editable = true;
        options.size = new Size();
        options.size.width = sz;
        options.size.height = sz;
        Rect rect = new Rect();
        rect.minX = 0;
        rect.minY = 0;
        rect.width = sz;
        rect.height = sz;
        return PixelMap.create(pm, rect, options);
    }


    static public PixelMap[][][] cutPicIntoN(PixelMap pm, int n) { // cut pic into nxn
        PixelMap[][][] ret = new PixelMap[2][n][n];
        ImageInfo imginfo = pm.getImageInfo();

        int h = imginfo.size.height;
        int w = imginfo.size.width;

        PixelMap.InitializationOptions options = new PixelMap.InitializationOptions();
        options.pixelFormat = imginfo.pixelFormat;
        options.editable = true;
        options.size = new Size();
        options.size.width = w;
        options.size.height = h;

        int pieceSizeH = (int) Math.floor((double) h / n);
        int pieceSizeW = (int) Math.floor((double) w / n);


        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                int startX = j * pieceSizeW;
                int startY = i * pieceSizeH;
                Rect rect = new Rect();
                rect.minX = startX;
                rect.minY = startY;
                rect.height = pieceSizeH;
                rect.width = pieceSizeW;
                PixelMap temp = PixelMap.create(pm, rect, options);
                PixelMap temp2 = PixelMap.create(pm, rect, options);
                Canvas canvas = new Canvas(new Texture(temp2));
                Paint paint = new Paint();
                paint.setTextSize(pieceSizeH * 2);
                paint.setColor(Color.RED);
                canvas.drawText(paint, "" + (i * n + j + 1), pieceSizeH, pieceSizeH * 2);
                ret[0][i][j] = temp;
                ret[1][i][j] = temp2;
            }
        }
        return ret;
    }
}
