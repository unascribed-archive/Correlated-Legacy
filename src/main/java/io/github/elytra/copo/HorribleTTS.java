package io.github.elytra.copo;

import javax.sound.sampled.AudioFormat;

import com.google.common.primitives.Shorts;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.JavaStreamingAudioPlayer;
import com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory;

public class HorribleTTS {
	
	public static Voice createVoice() {
		VoiceManager vm = VoiceManager.getInstance();
		Voice v = vm.getVoice("kevin");
		v.allocate();
		return v;
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("freetts.voices", KevinVoiceDirectory.class.getName());
		Voice v = createVoice();
		//AudioPlayer def = new SingleFileAudioPlayer("abomination", AudioFileFormat.Type.WAVE);
		AudioPlayer def = new JavaStreamingAudioPlayer();
		v.setAudioPlayer(new AudioPlayer() {
			
			@Override
			public boolean write(byte[] arr, int ofs, int len) {
				for (int i = ofs; i < ofs+len; i += 2) {
					int sample = (arr[i] << 8) | arr[i+1];
					
					//sample += (Math.sin(getTime()/300f)+1)*10000;
					
					sample = Shorts.saturatedCast(sample);
					
					arr[i] = (byte)((sample >> 8) & 0xFF);
					arr[i+1] = (byte)(sample & 0xFF);
				}
				return def.write(arr, ofs, len);
			}
			
			@Override
			public boolean write(byte[] arr) {
				return write(arr, 0, arr.length);
			}
			
			@Override
			public void startFirstSampleTimer() {
				def.startFirstSampleTimer();
			}
			
			@Override
			public void showMetrics() {
				def.showMetrics();
			}
			
			@Override
			public void setVolume(float arg0) {
				def.setVolume(arg0);
			}
			
			@Override
			public void setAudioFormat(AudioFormat arg0) {
				AudioFormat f = new AudioFormat(arg0.getSampleRate()*1.25f, arg0.getSampleSizeInBits(), arg0.getChannels(), true, arg0.isBigEndian());
				def.setAudioFormat(f);
			}
			
			@Override
			public void resume() {
				def.resume();
			}
			
			@Override
			public void resetTime() {
				def.resetTime();
			}
			
			@Override
			public void reset() {
				def.reset();
			}
			
			@Override
			public void pause() {
				def.pause();
			}
			
			@Override
			public float getVolume() {
				return def.getVolume();
			}
			
			@Override
			public long getTime() {
				return def.getTime();
			}
			
			@Override
			public AudioFormat getAudioFormat() {
				return def.getAudioFormat();
			}
			
			@Override
			public boolean end() {
				return def.end();
			}
			
			@Override
			public boolean drain() {
				return def.drain();
			}
			
			@Override
			public void close() {
				def.close();
			}
			
			@Override
			public void cancel() {
				def.cancel();
			}
			
			@Override
			public void begin(int arg0) {
				def.begin(arg0);
			}
		});
		v.speak("This is the terrible abomination that I wound up with. Is it still intelligible? I can't tell because I've been messing with it for a while.");
		v.deallocate();
		v.getAudioPlayer().close();
	}
}
