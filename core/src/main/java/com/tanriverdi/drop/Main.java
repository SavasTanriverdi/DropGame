package com.tanriverdi.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;

    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    Vector2 touchPos;
    Array<Sprite> dropSprites;

    float dropTimer;

    Rectangle bucketRectangle;
    Rectangle dropRectangle;

    BitmapFont font;
    int score;

    @Override
    public void create() {
        // Kaynakları yükleme
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));

        // SpriteBatch ve Viewport oluşturma
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);

        touchPos = new Vector2();
        dropSprites = new Array<>();

        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        font = new BitmapFont();
        score = 0;

        // Müzik ayarları
        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        updateLogic();
        draw();
    }

    private void input() {
        // Kullanıcı girişlerini kontrol et
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);
        }

        // Dokunma girdisi
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            bucketSprite.setCenterX(touchPos.x);
        }

        // Kova sınırlarını kontrol et
        float bucketWidth = bucketSprite.getWidth();
        float worldWidth = viewport.getWorldWidth();
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));
    }

    private void updateLogic() {
        // Mantık işlemleri
        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketSprite.getWidth(), bucketSprite.getHeight());

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            dropSprite.translateY(-2f * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropSprite.getWidth(), dropSprite.getHeight());

            if (dropSprite.getY() < -dropSprite.getHeight()) {
                dropSprites.removeIndex(i);
            } else if (dropRectangle.overlaps(bucketRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
                score++;
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            spawnDroplet();
        }
    }

    private void draw() {
        // Ekranı temizle ve çizim yap
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        // Arka plan çizimi
        spriteBatch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        bucketSprite.draw(spriteBatch);

        // Damlalar çizimi
        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }

        // Puanı çiz
        font.setColor(Color.WHITE);
        font.getData().setScale(0.050f);
        font.draw(spriteBatch, "Score: " + score, 0.5f, viewport.getWorldHeight() - 0.5f);

        spriteBatch.end();
    }

    private void spawnDroplet() {
        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(1, 1);
        dropSprite.setX(MathUtils.random(0, viewport.getWorldWidth() - dropSprite.getWidth()));
        dropSprite.setY(viewport.getWorldHeight());
        dropSprites.add(dropSprite);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        backgroundTexture.dispose();
        bucketTexture.dispose();
        dropTexture.dispose();
        dropSound.dispose();
        music.dispose();
        font.dispose();
    }
}
