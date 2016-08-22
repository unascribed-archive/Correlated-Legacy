package io.github.elytra.copo;

import javax.sound.sampled.AudioFormat;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.JavaStreamingAudioPlayer;
import com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory;

import io.github.elytra.copo.repackage.sonic.Sonic;

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
			private Sonic sonic;
			@Override
			public boolean write(byte[] arr, int ofs, int len) {
				short[] samples = new short[len/2];
				for (int i = ofs; i < ofs+len; i += 2) {
					int byte1 = arr[i]&0xFF;
					int byte2 = arr[i+1]&0xFF;
					short sample = (short)((byte1 << 8) | (byte2 << 0));
					samples[i/2] = sample;
				}
				sonic.writeShortToStream(samples, len/2);
				byte[] processed = readFromSonic();
				return def.write(processed, 0, processed.length);
			}
			
			private byte[] readFromSonic() {
				short[] samples = new short[sonic.samplesAvailable()];
				int amt = sonic.readShortFromStream(samples, samples.length);
				byte[] processed = new byte[samples.length*2];
				int crushFactor = 12;
				for (int i = 0; i < amt; i++) {
					short sample = samples[i];
					sample = (short) ((sample >>> crushFactor) << crushFactor);
					processed[i*2] = (byte)((sample >>> 8) & 0xFF);
					processed[(i*2)+1] = (byte)(sample & 0xFF);
				}
				return processed;
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
				AudioFormat f = new AudioFormat(arg0.getSampleRate(), arg0.getSampleSizeInBits(), arg0.getChannels(), true, arg0.isBigEndian());
				sonic = new Sonic((int)f.getSampleRate(), f.getChannels());
				//sonic.setPitch(6f);
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
				sonic.flushStream();
				byte[] processed = readFromSonic();
				def.write(processed, 0, processed.length);
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
		v.speak("I might just go with a bitcrush instead of trying to pitch it up, I think it sucks enough");
		v.deallocate();
		v.getAudioPlayer().close();
	}
}
