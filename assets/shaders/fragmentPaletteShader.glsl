//palette rendering fragment shader
#version 330 core

in vec2 fragUV;

out vec4 pixel;

uniform int paletteWidth;
uniform int paletteHeight;
uniform sampler2D paletteTexture;
uniform sampler2D imageTexture;
uniform int channels;

void main() {

	vec4 imageSample = texture(imageTexture , fragUV);
	float adjustedU = (imageSample.r * paletteWidth) ;
	float adjustedV = (imageSample.g * paletteHeight) ;
	
	vec4 paletteRead = texture(paletteTexture , vec2(adjustedU / (paletteWidth) , adjustedV / (paletteHeight)));
	
	if(channels == 1) {

		float color = paletteRead.r;
		pixel = vec4(color , color , color , 1.0f);

	} else if (channels == 2) {

		float color = paletteRead.r;
		float alpha = paletteRead.g;
		pixel = vec4(color , color , color , alpha);

	} else pixel = paletteRead;

}