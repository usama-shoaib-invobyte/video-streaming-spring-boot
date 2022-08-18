package com.example.videostreaming.service;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class StreamingService {
    private static final String FORMAT = "classpath:videos/%s.mp4";

    @Autowired
    private ResourceLoader resourceLoader;

    public Mono<Resource> getVideo(String title) throws Exception{
        return Mono.fromSupplier(() -> this.resourceLoader.getResource(String.format(FORMAT, title)));
//        resourceLoader.getResource(String.format(FORMAT, title)).getInputStream();
//        byte[] data = Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(String.format(FORMAT, title)).toURI()));
//        return new ByteArrayResource(data).createRelative();
    }

    public Mono<Resource> getEncodedVideo(String title) throws Exception{
        FFmpeg ffmpeg = new FFmpeg("C:\\ffmpeg\\ffmpeg.exe");
        FFprobe ffprobe = new FFprobe("C:\\ffmpeg\\ffprobe.exe");

        FFmpegBuilder builder = new FFmpegBuilder()

                .setInput("D:\\spring projects\\video-streaming\\src\\main\\resources\\videos\\video.mp4")     // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists

                .addOutput("D:\\spring projects\\video-streaming\\src\\main\\resources\\videos\\output_video.mp4")   // Filename for the destination
                .setFormat("mp4")        // Format is inferred from filename, or can be set
//                .setTargetSize(250_000)  // Aim for a 250KB file

                .disableSubtitle()       // No subtiles

                .setAudioChannels(1)         // Mono audio
                .setAudioCodec("aac")        // using the aac codec
                .setAudioSampleRate(48_000)  // at 48KHz
                .setAudioBitRate(32768)      // at 32 kbit/s

                .setVideoCodec("libx264")     // Video using x264
                .setVideoFrameRate(24, 1)     // at 24 frames per second
                .setVideoResolution(256, 144) // at 640x480 resolution

                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

// Run a one-pass encode
        executor.createJob(builder).run();

// Or run a two-pass encode (which is better quality at the cost of being slower)
//        executor.createTwoPassJob(builder).run();
        return Mono.fromSupplier(() -> this.resourceLoader.getResource(String.format(FORMAT, "output_video")));
    }

}
