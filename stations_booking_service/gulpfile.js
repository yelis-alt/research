const {src, dest, watch, parallel, series} = require('gulp');
const scss = require('gulp-sass')(require('sass'));
const concat = require('gulp-concat');
const autoprefixer = require('gulp-autoprefixer');
const uglify = require('gulp-uglify');
const imagemin = require('gulp-imagemin');
const del = require('del');

function styles() {
    return src(['app/frontend/css/reset.scss',
		        'app/frontend/css/style.scss'])
    .pipe(scss({outputStyle: 'compressed'}))
    .pipe(concat('style.min.css'))
    .pipe(autoprefixer({
        overrideBrowserlist: ['last 10 versions'],
        grid: true
    }))
    .pipe(dest('app/frontend/css'))
}

function scripts() {
    return src([
        'node_modules/jquery/dist/jquery.js',
        'app/frontend/js/*.js',
        '!app/frontend/js/main.min.js'
    ])
    .pipe(concat('main.min.js'))
    .pipe(uglify())
    .pipe(dest('app/frontend/js'))
}

function images() {
    return src('app/frontend/images/**/*.*')
    .pipe(imagemin([imagemin.gifsicle({interlaced: true}),
	imagemin.mozjpeg({quality: 75, progressive: true}),
	imagemin.optipng({optimizationLevel: 5}),
	imagemin.svgo({
		plugins: [
			{removeViewBox: true},
			{cleanupIDs: false}
		]
	})]))
    .pipe(dest('dist/frontend/images'))
}

function build() {
   return src(['app/*.php',
               'app/*.json',
               'app/src/*',
               'app/src/templates/main.php',
               'app/backend/python/*.py',
               'app/frontend/css/style.min.css',
               'app/frontend/js/main.min.js',
               'app/frontend/fonts/*.ttf'], {base:'app'})
    .pipe(dest('dist'))
}

function watching() {
    watch(['app/frontend/css/**/*.scss'], styles);
    watch(['app/frontend/js/**/*.js',
           '!app/frontend/js/main.min.js'], scripts);
}

function cleanDist(){
    return del('dist')
}

exports.styles = styles;
exports.scripts = scripts;
exports.watching = watching;
exports.images = images;
exports.build = build;
exports.cleanDist = cleanDist;

exports.build = series(cleanDist, images, build);

exports.watch = parallel(styles, scripts, watching);