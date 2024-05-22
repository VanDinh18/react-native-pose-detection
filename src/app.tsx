/* eslint-disable react-native/no-inline-styles */
import React, {useEffect} from 'react';
import {StyleSheet, View} from 'react-native';
import {
  Camera,
  runAsync,
  useCameraDevice,
  useCameraFormat,
  useCameraPermission,
  useFrameProcessor,
} from 'react-native-vision-camera';
import {xyz} from './pose-processer';

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
      {device && hasPermission && (
        <Camera
          style={{
            position: 'absolute',
            width: widthCamera,
            height: heightCamera,
            bottom: 20,
            left: 20,
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
    </View>
  );
};

export default App;
