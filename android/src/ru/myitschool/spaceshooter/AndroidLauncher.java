package ru.myitschool.spaceshooter;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import ru.myitschool.spaceshooter.SpaceShooter;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new SpaceShooter(), config);
		// включить гироскоп
		config.useGyroscope = true;  // по умолчанию false

		// отключить датчики, которые включены по умолчанию, если они больше не будут нужны
		config.useAccelerometer = false;
		config.useCompass = false;

	}
}
