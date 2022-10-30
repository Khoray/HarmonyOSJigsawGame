package com.khoray.jigsawgame.slice;

import com.khoray.jigsawgame.ResourceTable;
import com.khoray.jigsawgame.utils.PicCutter;
import com.khoray.jigsawgame.utils.TimeToStrUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.IDialog;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;

import java.util.*;

public class GameAbilitySlice extends AbilitySlice {
    static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x0001, "动画测试");
    Image imageArray[];
    int imgId[];
    int diff;
    int pieceSizePx;
    int maxsiz;
    boolean noteFlag = false;
    Text timeText;
    Timer timer;
    int timeCount = 0;
    Button stopGameBtn, startGameBtn, noteBtn;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        DirectionalLayout imgLayout = new DirectionalLayout(getContext());
        imgLayout.setWidth(ComponentContainer.LayoutConfig.MATCH_PARENT);
        imgLayout.setHeight(ComponentContainer.LayoutConfig.MATCH_PARENT);
        imgLayout.setAlignment(LayoutAlignment.CENTER);
        imgLayout.setOrientation(Component.VERTICAL);
        imgLayout.setPaddingTop(AttrHelper.vp2px(20, getContext()));

        PixelMap pic = (PixelMap) intent.getSequenceableParam("pic");
        diff = intent.getIntParam("difficulty", 2);

        PixelMap[][][] curPixelMap = PicCutter.cutPicIntoN(pic, diff);
        int pieceSize = (int) Math.round(300.0 / diff);

        pieceSizePx = AttrHelper.vp2px(pieceSize, getContext());
        maxsiz = AttrHelper.vp2px(300, getContext());;

        imageArray = new Image[diff * diff];
        imgId = new int[diff * diff];
        DirectionalLayout tmpline = new DirectionalLayout(getContext());
        tmpline.setWidth(maxsiz);
        tmpline.setHeight(maxsiz);
        tmpline.setOrientation(Component.HORIZONTAL);
        ShapeElement ele = new ShapeElement();
        ele.setRgbColor(new RgbColor(160,160,160));
        tmpline.setBackground(ele);

        for(int i = 0; i < diff; i++) {
            for(int j = 0; j < diff; j++) {
                Image image = new Image(getContext());
                image.setScaleMode(Image.ScaleMode.STRETCH);
                image.setWidth(pieceSizePx);
                image.setHeight(pieceSizePx);
                image.setPixelMap(curPixelMap[0][i][j]);
                image.setPadding(10, 10, 10, 10);
                image.setVisibility(Component.INVISIBLE);
                imageArray[i * diff + j] = image;
                imgId[i * diff + j] = i * diff + j;
                image.setClickedListener(new slideImageListener());
                tmpline.addComponent(image);
            }
        }
        imgLayout.addComponent(tmpline);

//

        DirectionalLayout dl = (DirectionalLayout) LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_ability_game_btns, null, false);
        imgLayout.addComponent(dl);


        setUIContent(imgLayout);
        Image smallImg = (Image) findComponentById(ResourceTable.Id_small_img);
        smallImg.setPixelMap(pic);

        startGameBtn = (Button) findComponentById(ResourceTable.Id_start_btn);
        stopGameBtn = (Button) findComponentById(ResourceTable.Id_stop_btn);
        noteBtn = (Button) findComponentById(ResourceTable.Id_note_btn);

        stopGameBtn.setEnabled(false);
        stopGameBtn.setTextColor(Color.GRAY);
        noteBtn.setEnabled(false);
        noteBtn.setTextColor(Color.GRAY);

        timeText = (Text) findComponentById(ResourceTable.Id_time_text);

        startGameBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                startNewGame();
            }
        });

        stopGameBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                resetGame();
            }
        });

        noteBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                for(int i = 0; i < diff * diff - 1; i++) {
                    imageArray[i].setPixelMap(curPixelMap[noteFlag ? 0 : 1][i / diff][i % diff]);
                }
                noteFlag = !noteFlag;
                if(noteFlag) {
                    noteBtn.setText("关闭提示");
                } else {
                    noteBtn.setText("开启提示");
                }
            }
        });

    }

    private void checkEnd() {
        boolean end = true;
        for(int i = 0; i < diff * diff; i++) {
            if(imgId[i] != i) {
                end = false;
            }
        }
        if(end) {
            CommonDialog dialog = new CommonDialog(getContext());
            dialog.setTitleText("恭喜你！");
            dialog.setContentText("恭喜你成功完成拼图，用时：" + TimeToStrUtil.t2s(timeCount));
            dialog.setButton(IDialog.BUTTON3, "CONFIRM", (iDialog, i) -> iDialog.destroy());
            dialog.setDestroyedListener(() -> {
               resetGame();
            });
            dialog.show();
        }
    }



    private void createTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeCount++;
                getUITaskDispatcher().asyncDispatch(new Runnable() {
                    @Override
                    public void run() {
                        timeText.setText("用时：" + TimeToStrUtil.t2s(timeCount));
                    }
                });
            }
        }, 0, 1000);
        HiLog.info(label,  "fuck");
    }

    private void startNewGame() {
        startGameBtn.setEnabled(false);
        startGameBtn.setTextColor(Color.GRAY);
        stopGameBtn.setEnabled(true);
        stopGameBtn.setTextColor(Color.BLACK);
        noteBtn.setEnabled(true);
        noteBtn.setTextColor(Color.BLACK);
        createTimer();
        List<Integer> sf = new ArrayList<>();
        for(int i = 0; i < diff * diff; i++) {
            sf.add(i);
        }
        Collections.shuffle(sf);
        for(int i = 0; i < diff * diff; i++) {
            imgId[i] = sf.get(i);
            imageArray[imgId[i]].setContentPosition(i % diff * pieceSizePx, i / diff * pieceSizePx);
            imageArray[imgId[i]].setVisibility(Component.VISIBLE);
        }
        imageArray[diff * diff - 1].setVisibility(Component.INVISIBLE);
    }
    private void stopTimer() {
        timer.cancel();
        timer = null;
    }
    private void resetGame() {
        stopTimer();
        timeCount = 0;
        timeText.setText("用时：00:00");
        startGameBtn.setEnabled(true);
        startGameBtn.setTextColor(Color.BLACK);
        stopGameBtn.setEnabled(false);
        stopGameBtn.setTextColor(Color.GRAY);
        noteBtn.setEnabled(false);
        noteBtn.setTextColor(Color.GRAY);
        for(int i = 0; i < diff * diff; i++) {
            imgId[i] = i;
            imageArray[i].setContentPosition(i % diff * pieceSizePx, i / diff * pieceSizePx);
            imageArray[i].setVisibility(Component.VISIBLE);
        }
    }

    private void getNote() {
        for(int i = 0; i < diff * diff; i++) {
            imgId[i] = i;
            imageArray[i].setContentPosition(i % diff * pieceSizePx, i / diff * pieceSizePx);
            imageArray[i].setVisibility(Component.VISIBLE);
        }
    }


    class slideImageListener implements Component.ClickedListener {
        @Override
        public void onClick(Component component) {
            // 找位置
            HiLog.info(label, "onclick");
            int id = 0;
            for(int i = 0; i < diff * diff; i++) {
                if(imageArray[i] == component) {
                    id = i;
                    break;
                }
            }
            HiLog.info(label, "after pos, id:" + id);
            int pos = 0;
            for(int i = 0; i < diff * diff; i++) {
                if(imgId[i] == id) {
                    pos = i;
                }
            }
            HiLog.info(label, "pos:" + pos);
            // 找四个方向
            int d[] = new int[] {-diff, diff, 1, -1};
            for(int dir = 0; dir < 4; dir++) {
                int npos = pos + d[dir];
                HiLog.info(label, "test");
                if(npos < 0 || npos >= diff * diff) continue;
                if(imgId[npos] == diff * diff - 1) {
                    AnimatorProperty ani = imageArray[id].createAnimatorProperty();
                    float fromX = ani.getTarget().getContentPositionX();
                    float fromY = ani.getTarget().getContentPositionY();
                    float toX = imageArray[diff * diff - 1].getContentPositionX();
                    float toY = imageArray[diff * diff - 1].getContentPositionY();
                    HiLog.info(label, "fromX:" + fromX + " fromY:" + fromY + " toX:" + toX + " toY:" + toY);
                    ani.moveFromX(fromX)
                            .moveFromY(fromY)
                            .moveToX(toX)
                            .moveToY(toY);

                    imageArray[diff * diff - 1].setContentPosition(fromX, fromY);
                    imgId[pos] = diff * diff - 1;
                    imgId[npos] = id;
                    ani.setDuration(300);
                    ani.start();
                    break;
                }
            }
            checkEnd();
        }
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
