package com.josh_mckenzie.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainPong extends ApplicationAdapter {
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private BitmapFontLoader bitmapFontLoader;

    private Texture rectangle1Tex;
    private Sprite rectangle1;
    private Rectangle rectangle1BBox;
    private Label rectangle1ScoreLabel;
    private Integer p1Score;

    private Texture rectangle2Tex;
    private Sprite rectangle2;
    private Rectangle rectangle2BBox;
    private Label rectangle2ScoreLabel;
    private Integer p2Score;

    private Texture ballTex;
    private Sprite ball;
    private Rectangle ballBBox;

    private Texture controlsLabel;

    private Sound beepSound;
    private Sound boopSound;
    private Sound scoreSound;

//    private Texture testSquareTex;
//    private Sprite testSquare;

    private float gbuf = 4f; // gutterball buffer value for collisions
    private float ballSpeedXMin = 50f;
    private float ballSpeedXMax = 150f;
    private float ballSpeedYMin = 100f;
    private float ballSpeedYMax = 200f;
    private float randNegative;
    private float ballSpeedX;
    private float ballSpeedY;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        rectangle1Tex = new Texture("rectangle.png");
        rectangle2Tex = new Texture("rectangle.png");
        ballTex = new Texture("ball.png");
        controlsLabel = new Texture("Controls.png");
        float spaceFromEdgeY = 40f;

        beepSound = Gdx.audio.newSound(Gdx.files.internal("beep.wav"));
        boopSound = Gdx.audio.newSound(Gdx.files.internal("boop.wav"));
        scoreSound = Gdx.audio.newSound(Gdx.files.internal("score-sound.wav"));
//        beepSound.setVolume(0.5f);
//        boopSound.setVolume(0.5f);
//        scoreSound.setVolume(0.5f);
//        beepSound.setLooping(false);
//        boopSound.setLooping(false);
//        scoreSound.setLooping(false);

        p1Score = 0;
        p2Score = 0;

        rectangle1 = new Sprite(rectangle1Tex);
        rectangle1.setBounds(Gdx.graphics.getWidth() / 2 - rectangle1Tex.getWidth() / 2, spaceFromEdgeY,
            rectangle1Tex.getWidth(), rectangle1Tex.getHeight());
        rectangle1BBox = new Rectangle();

        rectangle1ScoreLabel = new Label(p1Score.toString(),
            new LabelStyle(
                new BitmapFont(
                new InternalFileHandleResolver().resolve("square-font.fnt")),
                Color.WHITE));
        rectangle1ScoreLabel.setFontScale(1f);
        rectangle1ScoreLabel.setPosition(Gdx.graphics.getWidth() - spaceFromEdgeY, 5);
        rectangle1ScoreLabel.setAlignment(Align.right);

        rectangle2 = new Sprite(rectangle2Tex);
        rectangle2.setBounds(Gdx.graphics.getWidth() / 2 - rectangle2Tex.getWidth() / 2,
            viewport.getWorldHeight() - spaceFromEdgeY - rectangle2.getHeight(), rectangle2Tex.getWidth(),
            rectangle2Tex.getHeight());
        rectangle2BBox = new Rectangle();

        rectangle2ScoreLabel = new Label(p2Score.toString(),
            new LabelStyle(
                new BitmapFont(
                    new InternalFileHandleResolver().resolve("square-font.fnt")),
                Color.WHITE));
        rectangle2ScoreLabel.setFontScale(1f);
        rectangle2ScoreLabel.setPosition(Gdx.graphics.getWidth() - spaceFromEdgeY, Gdx.graphics.getHeight() - 37);
        rectangle2ScoreLabel.setAlignment(Align.right);

        ball = new Sprite(ballTex);
        ballBBox = new Rectangle();
        resetBall();

//        testSquareTex = new Texture("test-square.png");
//        testSquare = new Sprite(testSquareTex);
//        testSquare.setBounds(0,0,testSquareTex.getWidth(), testSquareTex.getHeight());
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        float rectSpeed = 300f;
        float delta = Gdx.graphics.getDeltaTime();

        // rectangle 1
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            rectangle1.translateX(rectSpeed * delta);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            rectangle1.translateX(-rectSpeed * delta);
        }

        // rectangle 2
        if(Gdx.input.isKeyPressed(Keys.D)) {
            rectangle2.translateX(rectSpeed * delta);
        }
        if(Gdx.input.isKeyPressed(Keys.A)) {
            rectangle2.translateX(-rectSpeed * delta);
        }
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float delta = Gdx.graphics.getDeltaTime();

        // rectangle 1
        rectangle1.setX(MathUtils.clamp(rectangle1.getX(), 0f,
            worldWidth - rectangle1.getWidth()));

        // rectangle 2
        rectangle2.setX(MathUtils.clamp(rectangle2.getX(), 0f,
            worldWidth - rectangle2.getWidth()));

        // ball
        if(ball.getX() >= worldWidth - ball.getWidth() || ball.getX() <= 0f) {
            // This prevents the dot from getting trapped in the gutter on the x axis
            ball.translateX(ballSpeedX <= 0f ? gbuf : -gbuf);
            // bounce (reverse direction)
            ballSpeedX *= -1f * MathUtils.random(0.90f, 1.15f);
            boopSound.play();
        }
        if (ball.getY() >= worldHeight - ball.getHeight()) { // player 1 (bottom) scores on top
            rectangle1ScoreLabel.setText(++p1Score);
            scoreSound.play();
            resetBall();
        } else if (ball.getY() <= 0f) { //player 2 (top) scores on bottom
            rectangle2ScoreLabel.setText(++p2Score);
            scoreSound.play();
            resetBall();
        }

        rectangle1BBox.set(rectangle1.getX(), rectangle1.getY(), rectangle1.getWidth(), rectangle1.getHeight());
        rectangle2BBox.set(rectangle2.getX(), rectangle2.getY(), rectangle2.getWidth(), rectangle2.getHeight());
        ballBBox.set(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());

        if(ballBBox.overlaps(rectangle1BBox) || ballBBox.overlaps(rectangle2BBox)) {
            ballSpeedY *= -1.10f;
            beepSound.play();
        }

        ball.translateX(ballSpeedX * delta);
        ball.translateY(ballSpeedY * delta);
    }

    private void draw() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        spriteBatch.draw(controlsLabel, 2f, 2f);
        rectangle1.draw(spriteBatch);
        rectangle2.draw(spriteBatch);
        ball.draw(spriteBatch);

        rectangle1ScoreLabel.draw(spriteBatch, 1f);
        rectangle2ScoreLabel.draw(spriteBatch, 1f);

//        testSquare.draw(spriteBatch);

        spriteBatch.end();
    }

    private void resetBall() {
        ball.setBounds(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2,
            ballTex.getWidth(), ballTex.getHeight());
        randNegative = MathUtils.randomBoolean() ? 1 : -1;
        ballSpeedX = MathUtils.random(ballSpeedXMin, ballSpeedXMax) * randNegative;
        ballSpeedY = MathUtils.random(ballSpeedYMin, ballSpeedYMax) * randNegative;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // add lines of code
    }

    @Override
    public void resume() {
        // add lines of code here
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }
}
