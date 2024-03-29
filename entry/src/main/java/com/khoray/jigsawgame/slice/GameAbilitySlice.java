package com.khoray.jigsawgame.slice;

import com.khoray.jigsawgame.ResourceTable;
import com.khoray.jigsawgame.utils.MyShuffleUtil;
import com.khoray.jigsawgame.utils.PicCutter;
import com.khoray.jigsawgame.utils.TimeToStrUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.ElementScatter;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.IDialog;
import ohos.global.resource.NotExistException;
import ohos.global.resource.Resource;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;

import java.io.IOException;
import java.util.*;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_CONTENT;

public class GameAbilitySlice extends AbilitySlice {
    static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x0001, "动画测试");
    Image[] imageArray;
    int[] imgId;
    int diff;
    int pieceSizePx;
    int maxsiz;
    boolean noteFlag = false;
    boolean startFlag = false;
    Text timeText;
    Timer timer;
    int timeCount = 0;
    Button stopGameBtn, startGameBtn, noteBtn;
    PixelMap[][][] curPixelMap;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);

        DirectionalLayout imgLayout = new DirectionalLayout(getContext());
        imgLayout.setWidth(ComponentContainer.LayoutConfig.MATCH_PARENT);
        imgLayout.setHeight(ComponentContainer.LayoutConfig.MATCH_PARENT);
        imgLayout.setAlignment(LayoutAlignment.CENTER);
        imgLayout.setOrientation(Component.VERTICAL);
        imgLayout.setPaddingTop(AttrHelper.vp2px(20, getContext()));
        try {
            Resource resource = getContext().getResourceManager().getResource(ResourceTable.Media_bg);
            PixelMapElement pme = new PixelMapElement(resource);
            imgLayout.setBackground(pme);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }

        PixelMap pic = intent.getSequenceableParam("pic");
        diff = intent.getIntParam("difficulty", 2);

        curPixelMap = PicCutter.cutPicIntoN(pic, diff);
        int pieceSize = (int) Math.round(300.0 / diff);

        pieceSizePx = AttrHelper.vp2px(pieceSize, getContext());
        maxsiz = AttrHelper.vp2px(300, getContext());

        imageArray = new Image[diff * diff];
        imgId = new int[diff * diff];
        DependentLayout gameLayout = new DependentLayout(getContext());
        gameLayout.setWidth(maxsiz);
        gameLayout.setHeight(maxsiz);
        ShapeElement ele = new ShapeElement();
        ele.setRgbColor(new RgbColor(255,255,255, 52));
        gameLayout.setBackground(ele);

        for(int i = 0; i < diff; i++) {
            for(int j = 0; j < diff; j++) {
                Image image = new Image(getContext());
                image.setScaleMode(Image.ScaleMode.STRETCH);
                image.setWidth(pieceSizePx);
                image.setHeight(pieceSizePx);
                image.setPixelMap(curPixelMap[0][i][j]);
                image.setPadding(2, 2, 2, 2);
                image.setMarginTop(i * pieceSizePx);
                image.setMarginLeft(j * pieceSizePx);
//                image.setVisibility(Component.INVISIBLE);
                imageArray[i * diff + j] = image;
                imgId[i * diff + j] = i * diff + j;
                image.setClickedListener(new slideImageListener());
                gameLayout.addComponent(image);
            }
        }
        imgLayout.addComponent(gameLayout);

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

        startGameBtn.setClickedListener(component -> startNewGame());

        stopGameBtn.setClickedListener(component -> resetGame());

        noteBtn.setClickedListener(component -> noteGame());

    }

    private void noteGame() {
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

    private void checkEnd() {
        boolean end = true;
        for(int i = 0; i < diff * diff; i++) {
            if (imgId[i] != i) {
                end = false;
                break;
            }
        }
        if(end) {
            stopTimer();
            CommonDialog cd = new CommonDialog(getContext());

            DirectionalLayout dl = (DirectionalLayout) LayoutScatter.getInstance(getContext()).parse(ResourceTable.Layout_gameover_layout, null, false);
            Button replayBtn = (Button) dl.findComponentById(ResourceTable.Id_replay_btn);
            replayBtn.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component component) {
                    resetGame();
                    cd.destroy();
                }
            });

            Button backBtn = (Button) dl.findComponentById(ResourceTable.Id_back_btn);
            backBtn.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component component) {
                    terminate();
                    cd.destroy();
                }
            });
            Text playtimeText = (Text) dl.findComponentById(ResourceTable.Id_time_text);
            playtimeText.setText(timeText.getText());
            cd.setSize(600, MATCH_CONTENT);
            cd.setContentCustomComponent(dl);
            cd.show();
        }
    }



    private void createTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeCount++;
                getUITaskDispatcher().asyncDispatch(() -> timeText.setText("用时：" + TimeToStrUtil.t2s(timeCount)));
            }
        }, 0, 1000);
    }

    private void startNewGame() {
        startFlag = true;
        startGameBtn.setEnabled(false);
        startGameBtn.setTextColor(Color.GRAY);
        stopGameBtn.setEnabled(true);
        stopGameBtn.setTextColor(Color.WHITE);
        noteBtn.setEnabled(true);
        noteBtn.setTextColor(Color.WHITE);
        createTimer();
        for(int i = 0; i < diff * diff; i++) {
            imgId[i] = i;
        }
        MyShuffleUtil.shuffle(imgId, diff);
        for(int i = 0; i < diff * diff; i++) {
            imageArray[imgId[i]].setContentPosition(i % diff * pieceSizePx, (i / diff) * pieceSizePx);
            imageArray[imgId[i]].setVisibility(Component.VISIBLE);
        }
        imageArray[diff * diff - 1].setVisibility(Component.INVISIBLE);
    }
    private void stopTimer() {
        if(timer == null) return;
        timer.cancel();
        timer = null;
    }
    private void resetGame() {
        if(noteFlag) noteGame();
        startFlag = false;
        stopTimer();
        timeCount = 0;
        timeText.setText("用时：00:00");
        startGameBtn.setEnabled(true);
        startGameBtn.setTextColor(Color.WHITE);
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
            if(!startFlag) {
                return;
            }
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
            int x = pos / diff, y = pos % diff;
            int d[] = new int[] {1, 0, -1, 0};
            for(int dir = 0; dir < 4; dir++) {
                int nx = x + d[dir], ny = y + d[3 - dir];
                if(nx < 0 || nx >= diff || ny < 0 || ny >= diff) continue;
                int npos = nx * diff + ny;
                HiLog.info(label, "test");

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
                    // 防止连续点动画出bug
                    imageArray[diff * diff - 1].setContentPosition(pos % diff * pieceSizePx, pos / diff * pieceSizePx);
                    imgId[pos] = diff * diff - 1;
                    imgId[npos] = id;
                    ani.setDuration(100);
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
