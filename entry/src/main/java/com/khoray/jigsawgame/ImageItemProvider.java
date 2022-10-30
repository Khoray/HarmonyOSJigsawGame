package com.khoray.jigsawgame;

import ohos.agp.components.*;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;


import java.util.List;

import com.khoray.jigsawgame.utils.PicCutter;

public class ImageItemProvider extends BaseItemProvider {
    static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x0001, "provider测试");
    List<Integer> imgList;
    Context ctx;
    ClickedListener listener;

    public void setListener(ClickedListener listener) { this.listener = listener; }

    public static interface ClickedListener {
        void click(int pos);
    }

    public ImageItemProvider(List<Integer> imgList, Context ctx) {
        this.imgList = imgList;
        this.ctx = ctx;
    }


    @Override
    public int getCount() {
        HiLog.info(label, "getCount:" + imgList.size()); return imgList.size();
    }

    @Override
    public Object getItem(int i) {
        return imgList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Component getComponent(int i, Component component, ComponentContainer componentContainer) {
        DirectionalLayout dl = (DirectionalLayout) LayoutScatter.getInstance(ctx).parse(ResourceTable.Layout_img_list_item, null, false);
        Image img = (Image) dl.findComponentById(ResourceTable.Id_img_list_item);
        img.setPixelMap((Integer) getItem(i));
        PixelMap pm = img.getPixelMap();
        img.setPixelMap(PicCutter.rescale(pm));
        HiLog.info(label, "getComponent:" + i + " ComponentPosition:[" + img.getContentPositionX() + ", " + img.getContentPositionY() + "]");

        img.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                listener.click(i);
            }
        });

        return dl;
    }
}
