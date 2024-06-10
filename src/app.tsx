/* eslint-disable react-native/no-inline-styles */
import React, {useEffect} from 'react';
import {
  StyleSheet,
  View,
  ViewProps,
  requireNativeComponent,
} from 'react-native';
import {
  Camera,
  runAsync,
  useCameraDevice,
  useCameraFormat,
  useCameraPermission,
  useFrameProcessor,
} from 'react-native-vision-camera';
import {xyz} from './pose-processer';
import Video, {PosterResizeModeType, ResizeMode} from 'react-native-video';

type PoseGraphicOverlayProps = ViewProps & {
  strokeSize?: number;
};

const PoseGraphicOverlay =
  requireNativeComponent<PoseGraphicOverlayProps>('PoseGraphicOverlay');

const VIDEO_URL =
  'https://applillius.s3.ap-northeast-2.amazonaws.com/obtdev/training/20240213_120754_351_1.mp4';
const IMAGE_URL =
  'https://applillius.s3.ap-northeast-2.amazonaws.com/obtdev/training/20240213_120844_467_1.jpg';

const App = () => {
  const {hasPermission, requestPermission} = useCameraPermission();
  const widthCamera = 120;
  const heightCamera = 200;
  const targetFps = 30;
  const cameraAspectRatio = heightCamera / widthCamera;

  const device = useCameraDevice('front');
  const format = useCameraFormat(device, [
    {fps: targetFps},
    {videoAspectRatio: cameraAspectRatio},
    {videoResolution: 'max'},
    {photoAspectRatio: cameraAspectRatio},
    {photoResolution: 'max'},
  ]);

  const fps = Math.min(format?.maxFps ?? 1, targetFps);

  useEffect(() => {
    requestPermission();
  }, []);

  const frameProcessor = useFrameProcessor(frame => {
    'worklet';
    runAsync(frame, () => {
      'worklet';
      xyz(frame);
    });
  }, []);

  return (
    <View
      style={{
        flex: 1,
      }}>
      <Video
        source={{uri: VIDEO_URL}}
        poster={IMAGE_URL}
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          zIndex: 0,
        }}
        resizeMode={ResizeMode.COVER}
        posterResizeMode={PosterResizeModeType.COVER}
        paused={false}
        repeat={true}
        muted={true}
        playWhenInactive={false}
        hideShutterView
      />
      {device && hasPermission && (
        <Camera
          style={{
            position: 'absolute',
            width: widthCamera,
            height: heightCamera,
            bottom: 20,
            left: 20,
            zIndex: 1,
          }}
          frameProcessor={frameProcessor}
          pixelFormat="yuv"
          device={device}
          isActive={true}
          orientation="portrait"
          format={format}
          fps={fps}
          exposure={0}
        />
      )}
      <PoseGraphicOverlay
        style={{
          position: 'absolute',
          width: widthCamera,
          height: heightCamera,
          bottom: 20,
          left: 20,
          zIndex: 10,
          overflow: 'hidden',
        }}
      />
    </View>
  );
};

export default App;
