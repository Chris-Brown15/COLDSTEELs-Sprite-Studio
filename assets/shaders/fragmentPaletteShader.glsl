#version 330 core

const float paletteWidth = 256;
const float paletteHeight = 256;
const int GRAY_GRAYSCALE_SHADE = 0xffffffff;
const int RED_GRAYSCALE_SHADE = 0xff0000ff;
const int GREEN_GRAYSCALE_SHADE = 0x00ff00ff;
const int BLUE_GRAYSCALE_SHADE = 0x0000ffff;
const int YELLOW_GRAYSCALE_SHADE = 0xffff00ff;
const int CYAN_GRAYSCALE_SHADE = 0x00ffffff;
const int MAGENTA_GRAYSCALE_SHADE = 0xff00ffff;

in vec2 fragUV;

out vec4 pixel;

uniform sampler2D paletteTexture;
uniform sampler2D imageTexture;
uniform int channels;
uniform int grayscaleShade;

void main() {

	vec4 imageSample = texture(imageTexture , fragUV);
	uint adjustedU = uint(imageSample.r * 255);
	uint adjustedV = uint(imageSample.g * 255);
	
	vec4 paletteRead = texture(paletteTexture , vec2(adjustedU / paletteWidth , adjustedV / paletteHeight));

	if(channels <= 2) {

		float color = paletteRead.r;
		float alpha = channels == 2 ? paletteRead.g : 1.0f;

		switch(grayscaleShade) {
			case GRAY_GRAYSCALE_SHADE: pixel = vec4(color , color , color , alpha); break;
			case RED_GRAYSCALE_SHADE: pixel = vec4(color , 0 , 0 , alpha); break;
			case GREEN_GRAYSCALE_SHADE: pixel = vec4(0 , color , 0 , alpha); break;
			case BLUE_GRAYSCALE_SHADE: pixel = vec4(0 , 0 , color , alpha); break;
			case YELLOW_GRAYSCALE_SHADE: pixel = vec4(color , color , 0 , alpha); break;
			case CYAN_GRAYSCALE_SHADE: pixel = vec4(0 , color , color , alpha); break;
			case MAGENTA_GRAYSCALE_SHADE: pixel = vec4(color , 0 , color , alpha); break;		
		}


	} else pixel = paletteRead;

}